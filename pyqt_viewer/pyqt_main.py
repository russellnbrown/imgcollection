import os
import sys
from pathlib import Path

from PyQt5 import QtCore, QtWidgets
from PyQt5.QtWidgets import QApplication, QMainWindow, QLabel, QGridLayout, QWidget, QHBoxLayout, QVBoxLayout, \
    QPushButton, QFileSystemModel, QTreeView
from PyQt5.QtCore import QSize, pyqtSlot, QDir

from set import ImgDB

db:ImgDB = None

class ViewerWindow(QWidget):
    statusWindow:QLabel = None
    bottomStatus:QLabel = None
    fsModel:QFileSystemModel = None

    def makeTop(self):
        topBox = QHBoxLayout()
        self.statusWidget = QLabel('')
        self.statusWidget.setMinimumHeight(1)
        self.statusWidget.setMaximumHeight(1)
        self.statusWidget.setStyleSheet("QLabel { background-color : lightgray; color : black; }")
        topBox.addWidget(self.statusWidget)
        return topBox

    def makeExplorer(self, under):
        self.fsModel = QFileSystemModel()
        self.fsModel.setRootPath(QDir.currentPath());
        tree = QTreeView()
        tree.setModel(self.fsModel);
        return tree


    def makeMiddle(self):
        middleBox = QHBoxLayout()
        explorerWidget = self.makeExplorer(middleBox)
        #explorerWidget.setAlignment(QtCore.Qt.AlignCenter)
        explorerWidget.setMinimumHeight(300)
        explorerWidget.setMinimumWidth(300)
        explorerWidget.setMaximumWidth(400)
        explorerWidget.setMaximumHeight(20000)
        explorerWidget.setStyleSheet("QLabel { background-color : green; color : blue; }")
        filesWidget = QLabel("List")
        filesWidget.setAlignment(QtCore.Qt.AlignCenter)
        filesWidget.setStyleSheet("QLabel { background-color : blue; color : blue; }")
        filesWidget.setMinimumHeight(300)
        filesWidget.setMinimumWidth(300)
        filesWidget.setMaximumWidth(400)
        filesWidget.setMaximumHeight(20000)
        displayWidget = QLabel("Display")
        displayWidget.setAlignment(QtCore.Qt.AlignCenter)
        displayWidget.setStyleSheet("QLabel { background-color : pink; color : blue; }")
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
