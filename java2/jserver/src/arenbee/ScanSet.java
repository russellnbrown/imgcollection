/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package arenbee;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.apache.commons.lang3.tuple.Pair;

/**
 *
 * @author russ
 */
public class ScanSet {

    public final ConcurrentHashMap<Long, ImgCollectionDirItem> dirs = new ConcurrentHashMap<>();
    public final ConcurrentLinkedQueue<ImgCollectionFileItem> files = new ConcurrentLinkedQueue<>();
    public final ConcurrentHashMap<Long, ImgCollectionImageItem> images = new ConcurrentHashMap<>();
    public String top;

    public List<DirSearchResult> matchingDirs(String srch, int limit) {
        List<DirSearchResult> l = new LinkedList<DirSearchResult>();

        for (ImgCollectionDirItem i : dirs.values()) {
            if (org.apache.commons.lang3.StringUtils.containsIgnoreCase(i.getDir(), srch)) {
                l.add(new DirSearchResult(i.getDir()));
            }
            if (l.size() == limit) {
                break;
            }
        }
        return l;
    }

    public List<Pair<String, String>> matchingFiles(String srch, int limit) {
        List<Pair<String, String>> res = new LinkedList<Pair<String, String>>();

        for (ImgCollectionFileItem i : files) {
            if (org.apache.commons.lang3.StringUtils.containsIgnoreCase(i.getFile(), srch)) {
                try {
                    String dname = dirs.get(i.getDhash()).getDir();
                    res.add(Pair.of(dname, i.getFile()));
                } catch (Exception ex) {

                }
            }
            if (res.size() == limit) {
                break;
            }
        }

        return res;
    }

    public ScanSet(String rootPath) {
        Path s = Paths.get(rootPath);

        if (!Files.exists(s)) {
            System.out.println("No Set Found");
        }

        System.out.println("Opening "+ rootPath);

        int lc = 0;
        int f = 0;

        try {
            Path pdir = s.resolve("dirs.txt");
            Path pfile = s.resolve("files.txt");
            Path puniq = s.resolve("images.bin");

            Scanner fdirs;
            Scanner ffiles;
            FileInputStream fin;
            FileChannel funiq;

            fin = new FileInputStream(puniq.toString());
            funiq = fin.getChannel();
            fdirs = new Scanner(pdir);
            ffiles = new Scanner(pfile);

            String line;
            line = fdirs.nextLine();
            top = line;

            while (fdirs.hasNext()) {
                line = fdirs.nextLine();
                f = 1;
                lc++;
                try {
                    String[] parts = line.split("\\|");

                    ImgCollectionDirItem sid = new ImgCollectionDirItem();
                    sid.setDir(parts[1]);
                    sid.setHash(Long.parseLong(parts[0]));
                    sid.setLmod(Long.parseLong(parts[2]));

                    dirs.put(sid.getHash(), sid);
                } catch (NumberFormatException nfe) {
                    System.out.println("Number ex reading dirs" + nfe.getMessage() + line);
                }
            }

            f = 2;
            lc = 0;
            long fpos = 0;
            try {
                while (ffiles.hasNext()) {
                    line = ffiles.nextLine();
                    f = 2;
                    lc++;
                    String[] fparts = line.split(",");

                    try {
                        long dhash = Long.parseLong(fparts[0]);
                        long crc = Long.parseLong(fparts[1]);
                        ImgCollectionFileItem sif = new ImgCollectionFileItem();
                        sif.setIHash(crc);
                        sif.setDhash(dhash);
                        sif.setFile(fparts[2]);
                        files.add(sif);
                    } catch (NumberFormatException nfe) {
                        System.out.println("NUmber ex reading files" + nfe.getMessage() + line);
                    } catch (Exception nfe) {
                        System.out.println("NUmber ex reading files" + nfe.getMessage() + line);
                    }

                    //line = ffiles.readLine();
                }
            } catch (Exception e) // sometimes filename have unprintable characters - ignore them
            {
                System.out.println("Bad readline, malformed");
                return;
            }

            lc = 0;
            f = 3;
            ByteBuffer ibuf = ByteBuffer.allocateDirect(8 + 3 * 16 * 16);
            ibuf.order(ByteOrder.LITTLE_ENDIAN);

            try {
                while (fin.available() > 0) {
                    lc++;
                    ibuf.rewind();
                    funiq.read(ibuf);
                    ibuf.rewind();
                    long crc = ibuf.getLong();
                    byte[] buf = new byte[16 * 16 * 3];
                    ibuf.get(buf);
                    ImgCollectionImageItem sii = new ImgCollectionImageItem();
                    sii.setCrc(crc);
                    sii.setThumb(buf);
                    images.put(crc, sii);
                }
            } catch (IOException e) {

            }
            fdirs.close();
            ffiles.close();
            funiq.close();
            fin.close();

        } catch (IOException ioe) {
            ioe.printStackTrace();
            System.out.println(String.format("Caught IOE : %s in save lc=%d, f=%d", ioe.toString(), lc, f));
        }
        System.out.println(String.format("Set has %d dirs, %d files and %d images.", dirs.size(), files.size(), images.size()));
    }

}
