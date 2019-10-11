import os
import sys
from pathlib import Path

from PyQt5 import QtCore, QtWidgets
from PyQt5.QtGui import QDesktopServices
from PyQt5.QtWidgets import QApplication, QMainWindow, QLabel, QGridLayout, QWidget, QHBoxLayout, QVBoxLayout, \
    QPushButton, QFileSystemModel, QTreeView, QListView
from PyQt5.QtCore import QSize, pyqtSlot, QDir, QModelIndex

from set import ImgDB

db:ImgDB = None

class ViewerWindow(QWidget):
    statusWindow:QLabel = None
    bottomStatus:QLabel = None
    fsDirModel:QFileSystemModel = None
    fsFileModel:QFileSystemModel = None
    fsTree:QTreeView = None
    fsList:QListView = None

    def makeTop(self):
        topBox = QHBoxLayout()
        self.statusWidget = QLabel('')
        self.statusWidget.setMinimumHeight(1)
        self.statusWidget.setMaximumHeight(1)
        self.statusWidget.setStyleSheet("QLabel { background-color : lightgray; color : black; }")
        topBox.addWidget(self.statusWidget)
        return topBox

    @pyqtSlot(QModelIndex)
    def onDirChange(self, index):
        mPath = self.fsDirModel.fileInfo(index).absoluteFilePath()
        self.fsList.setRootIndex(self.fsFileModel.setRootPath(mPath));
        print("Dir=%s", mPath)

    def makeExplorer(self, under):
        self.fsDirModel = QFileSystemModel()
        self.fsDirModel.setFilter(QDir.Dirs|QDir.Drives|QDir.NoDotAndDotDot|QDir.AllDirs)
        self.fsDirModel.setRootPath("");
        self.fsTree = QTreeView()
        self.fsTree.setModel(self.fsDirModel);
        self.fsTree.clicked.connect(self.onDirChange)
        self.fsTree.hideColumn(1)
        self.fsTree.hideColumn(2)
        self.fsTree.hideColumn(3)
        self.fsTree.hideColumn(4)
        self.fsTree.setStyleSheet("QLabel { background-color : green; color : blue; }")
        return self.fsTree

    def makeFiles(self, under):
        self.fsFileModel = QFileSystemModel()
        self.fsFileModel.setFilter(QDir.NoDotAndDotDot|QDir.Files)
        self.fsFileModel.setRootPath("");
        self.fsList = QListView()
        self.fsList.setModel(self.fsFileModel)
        self.fsList.setStyleSheet("QLabel { background-color : lightblue; color : blue; }")
        return self.fsList

    def makeDisplay(selfself, under):
        displayWidget = QLabel("Display")
        displayWidget.setAlignment(QtCore.Qt.AlignCenter)
        displayWidget.setStyleSheet("QLabel { background-color : pink; color : blue; }")
        return displayWidget

    def makeMiddle(self):
        middleBox = QHBoxLayout()
        explorerWidget = self.makeExplorer(middleBox)
        explorerWidget.setMinimumHeight(300)
        explorerWidget.setMinimumWidth(300)
        explorerWidget.setMaximumWidth(400)
        explorerWidget.setMaximumHeight(20000)

        filesWidget = self.makeFiles(middleBox)
        filesWidget.setMinimumHeight(300)
        filesWidget.setMinimumWidth(300)
        filesWidget.setMaximumWidth(400)
        filesWidget.setMaximumHeight(20000)

        displayWidget = self.makeDisplay(middleBox)
        displayWidget.setMinimumHeight(300)
        displayWidget.setMinimumWidth(300)
        displayWidget.setMaximumHeight(20000)

        middleBox.addWidget(explorerWidget)
        middleBox.addWidget(filesWidget)
        middleBox.addWidget(displayWidget)
        return middleBox

    @pyqtSlot()
    def okOK(self):
        print("Clicked")

    def makeBottom(self ):
        bottomBox = QHBoxLayout()
        self.bottomStatus = QLabel("Status")
        self.bottomStatus.setMaximumHeight(20)
        self.bottomStatus.setMinimumHeight(20)
        self.bottomStatus.setStyleSheet("QLabel { background-color : lightgray; color : black; }")
        bottomBox.addWidget(self.bottomStatus)
        bottomOK = QPushButton("Quit",self)
        bottomOK.setMaximumWidth(80)
        bottomOK.setToolTip("Quit Application")
        bottomOK.clicked.connect(self.okOK)
        bottomBox.addWidget(bottomOK)
        return bottomBox

    def __init__(self):
        super().__init__()

        vbox = QVBoxLayout()
        vbox.addLayout(self.makeTop())
        vbox.addLayout(self.makeMiddle())
        vbox.addLayout(self.makeBottom())

        self.setLayout(vbox)
        self.setWindowTitle("QtText")
        self.setGeometry(200,200,800,600)


if __name__ == "__main__":
    app = QtWidgets.QApplication(sys.argv)
    db = ImgDB()
    db.load(Path('C:/TestEnvironments/img/setcee.db'))

    mainWin = ViewerWindow()
    mainWin.show()
    mainWin.bottomStatus.setText(db.getStatus())

    sys.exit(app.exec_())
