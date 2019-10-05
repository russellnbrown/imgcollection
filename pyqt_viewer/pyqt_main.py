import sys
from PyQt5 import QtCore, QtWidgets
from PyQt5.QtWidgets import QMainWindow, QLabel, QGridLayout, QWidget, QHBoxLayout, QVBoxLayout, QPushButton
from PyQt5.QtCore import QSize


class HelloWindow(QWidget):
    def __init__(self):
        super().__init__()

        label = QLabel("Label1")
        label.setAlignment(QtCore.Qt.AlignCenter)

        okbtn = QPushButton("OK")
        ccancelbtn = QPushButton("Cancel")

        hbox1 = QHBoxLayout()
        hbox1.addWidget(label)

        hbox2 = QHBoxLayout()
        hbox2.addWidget(okbtn)
        hbox2.addWidget(ccancelbtn)

        vbox = QVBoxLayout()
        vbox.addStretch(1)
        vbox.addLayout(hbox1)
        vbox.addLayout(hbox2)

        self.setLayout(vbox)
        self.setWindowTitle("QtText")
        self.setGeometry(300,300,300,120)





if __name__ == "__main__":
    app = QtWidgets.QApplication(sys.argv)
    mainWin = HelloWindow()
    mainWin.show()
    sys.exit(app.exec_())
