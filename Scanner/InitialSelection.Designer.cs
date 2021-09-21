
namespace Scanner
{
    partial class InitialSelection
    {
        /// <summary>
        /// Required designer variable.
        /// </summary>
        private System.ComponentModel.IContainer components = null;

        /// <summary>
        /// Clean up any resources being used.
        /// </summary>
        /// <param name="disposing">true if managed resources should be disposed; otherwise, false.</param>
        protected override void Dispose(bool disposing)
        {
            if (disposing && (components != null))
            {
                components.Dispose();
            }
            base.Dispose(disposing);
        }

        #region Windows Form Designer generated code

        /// <summary>
        /// Required method for Designer support - do not modify
        /// the contents of this method with the code editor.
        /// </summary>
        private void InitializeComponent()
        {
            this.components = new System.ComponentModel.Container();
            this.openExistingBtn = new System.Windows.Forms.Button();
            this.groupBox1 = new System.Windows.Forms.GroupBox();
            this.groupBox2 = new System.Windows.Forms.GroupBox();
            this.chooseTop = new System.Windows.Forms.Button();
            this.label3 = new System.Windows.Forms.Label();
            this.label2 = new System.Windows.Forms.Label();
            this.openExisting = new System.Windows.Forms.Button();
            this.setLocation = new System.Windows.Forms.TextBox();
            this.relativeTo = new System.Windows.Forms.TextBox();
            this.createBtn = new System.Windows.Forms.Button();
            this.scanGB = new System.Windows.Forms.GroupBox();
            this.label8 = new System.Windows.Forms.Label();
            this.dupFilesTB = new System.Windows.Forms.TextBox();
            this.label7 = new System.Windows.Forms.Label();
            this.dupDirTB = new System.Windows.Forms.TextBox();
            this.label6 = new System.Windows.Forms.Label();
            this.imageCountTB = new System.Windows.Forms.TextBox();
            this.label5 = new System.Windows.Forms.Label();
            this.fileCountTB = new System.Windows.Forms.TextBox();
            this.label4 = new System.Windows.Forms.Label();
            this.directoryCountTB = new System.Windows.Forms.TextBox();
            this.scannerStatus = new System.Windows.Forms.TextBox();
            this.startBtn = new System.Windows.Forms.Button();
            this.label1 = new System.Windows.Forms.Label();
            this.scanThisBtn = new System.Windows.Forms.Button();
            this.scanThis = new System.Windows.Forms.TextBox();
            this.monitorTimer = new System.Windows.Forms.Timer(this.components);
            this.curDirTB = new System.Windows.Forms.TextBox();
            this.curFileTB = new System.Windows.Forms.TextBox();
            this.groupBox1.SuspendLayout();
            this.groupBox2.SuspendLayout();
            this.scanGB.SuspendLayout();
            this.SuspendLayout();
            // 
            // openExistingBtn
            // 
            this.openExistingBtn.Location = new System.Drawing.Point(460, 36);
            this.openExistingBtn.Name = "openExistingBtn";
            this.openExistingBtn.Size = new System.Drawing.Size(95, 23);
            this.openExistingBtn.TabIndex = 0;
            this.openExistingBtn.Text = "Select";
            this.openExistingBtn.UseVisualStyleBackColor = true;
            this.openExistingBtn.Click += new System.EventHandler(this.OnSelectExisting);
            // 
            // groupBox1
            // 
            this.groupBox1.Controls.Add(this.openExistingBtn);
            this.groupBox1.Location = new System.Drawing.Point(12, 12);
            this.groupBox1.Name = "groupBox1";
            this.groupBox1.Size = new System.Drawing.Size(573, 84);
            this.groupBox1.TabIndex = 1;
            this.groupBox1.TabStop = false;
            this.groupBox1.Text = "Open Existing";
            // 
            // groupBox2
            // 
            this.groupBox2.Controls.Add(this.chooseTop);
            this.groupBox2.Controls.Add(this.label3);
            this.groupBox2.Controls.Add(this.label2);
            this.groupBox2.Controls.Add(this.openExisting);
            this.groupBox2.Controls.Add(this.setLocation);
            this.groupBox2.Controls.Add(this.relativeTo);
            this.groupBox2.Controls.Add(this.createBtn);
            this.groupBox2.Location = new System.Drawing.Point(12, 102);
            this.groupBox2.Name = "groupBox2";
            this.groupBox2.Size = new System.Drawing.Size(573, 114);
            this.groupBox2.TabIndex = 2;
            this.groupBox2.TabStop = false;
            this.groupBox2.Text = "Create New";
            // 
            // chooseTop
            // 
            this.chooseTop.Location = new System.Drawing.Point(421, 22);
            this.chooseTop.Name = "chooseTop";
            this.chooseTop.Size = new System.Drawing.Size(33, 23);
            this.chooseTop.TabIndex = 18;
            this.chooseTop.Text = "...";
            this.chooseTop.UseVisualStyleBackColor = true;
            this.chooseTop.Click += new System.EventHandler(this.OnSetRelativeTo);
            // 
            // label3
            // 
            this.label3.AutoSize = true;
            this.label3.Location = new System.Drawing.Point(11, 25);
            this.label3.Name = "label3";
            this.label3.Size = new System.Drawing.Size(66, 15);
            this.label3.TabIndex = 17;
            this.label3.Text = "Relative To:";
            // 
            // label2
            // 
            this.label2.AutoSize = true;
            this.label2.Location = new System.Drawing.Point(11, 72);
            this.label2.Name = "label2";
            this.label2.Size = new System.Drawing.Size(75, 15);
            this.label2.TabIndex = 16;
            this.label2.Text = "Set Location:";
            // 
            // openExisting
            // 
            this.openExisting.Location = new System.Drawing.Point(421, 69);
            this.openExisting.Name = "openExisting";
            this.openExisting.Size = new System.Drawing.Size(33, 23);
            this.openExisting.TabIndex = 14;
            this.openExisting.Text = "...";
            this.openExisting.UseVisualStyleBackColor = true;
            this.openExisting.Click += new System.EventHandler(this.OnSetSetLocation);
            // 
            // setLocation
            // 
            this.setLocation.Location = new System.Drawing.Point(92, 69);
            this.setLocation.Name = "setLocation";
            this.setLocation.Size = new System.Drawing.Size(330, 23);
            this.setLocation.TabIndex = 12;
            // 
            // relativeTo
            // 
            this.relativeTo.Location = new System.Drawing.Point(92, 22);
            this.relativeTo.Name = "relativeTo";
            this.relativeTo.Size = new System.Drawing.Size(330, 23);
            this.relativeTo.TabIndex = 11;
            this.relativeTo.Click += new System.EventHandler(this.OnSetRelativeTo);
            // 
            // createBtn
            // 
            this.createBtn.Location = new System.Drawing.Point(460, 70);
            this.createBtn.Name = "createBtn";
            this.createBtn.Size = new System.Drawing.Size(95, 23);
            this.createBtn.TabIndex = 0;
            this.createBtn.Text = "Create";
            this.createBtn.UseVisualStyleBackColor = true;
            this.createBtn.Click += new System.EventHandler(this.OnCreateNew);
            // 
            // scanGB
            // 
            this.scanGB.Controls.Add(this.curFileTB);
            this.scanGB.Controls.Add(this.curDirTB);
            this.scanGB.Controls.Add(this.label8);
            this.scanGB.Controls.Add(this.dupFilesTB);
            this.scanGB.Controls.Add(this.label7);
            this.scanGB.Controls.Add(this.dupDirTB);
            this.scanGB.Controls.Add(this.label6);
            this.scanGB.Controls.Add(this.imageCountTB);
            this.scanGB.Controls.Add(this.label5);
            this.scanGB.Controls.Add(this.fileCountTB);
            this.scanGB.Controls.Add(this.label4);
            this.scanGB.Controls.Add(this.directoryCountTB);
            this.scanGB.Controls.Add(this.scannerStatus);
            this.scanGB.Controls.Add(this.startBtn);
            this.scanGB.Controls.Add(this.label1);
            this.scanGB.Controls.Add(this.scanThisBtn);
            this.scanGB.Controls.Add(this.scanThis);
            this.scanGB.Location = new System.Drawing.Point(12, 222);
            this.scanGB.Name = "scanGB";
            this.scanGB.Size = new System.Drawing.Size(574, 401);
            this.scanGB.TabIndex = 3;
            this.scanGB.TabStop = false;
            this.scanGB.Text = "Scanner";
            // 
            // label8
            // 
            this.label8.AutoSize = true;
            this.label8.Location = new System.Drawing.Point(166, 162);
            this.label8.Name = "label8";
            this.label8.Size = new System.Drawing.Size(37, 15);
            this.label8.TabIndex = 31;
            this.label8.Text = "Dups:";
            // 
            // dupFilesTB
            // 
            this.dupFilesTB.Location = new System.Drawing.Point(209, 159);
            this.dupFilesTB.Name = "dupFilesTB";
            this.dupFilesTB.ReadOnly = true;
            this.dupFilesTB.Size = new System.Drawing.Size(60, 23);
            this.dupFilesTB.TabIndex = 30;
            // 
            // label7
            // 
            this.label7.AutoSize = true;
            this.label7.Location = new System.Drawing.Point(166, 133);
            this.label7.Name = "label7";
            this.label7.Size = new System.Drawing.Size(37, 15);
            this.label7.TabIndex = 29;
            this.label7.Text = "Dups:";
            // 
            // dupDirTB
            // 
            this.dupDirTB.Location = new System.Drawing.Point(209, 130);
            this.dupDirTB.Name = "dupDirTB";
            this.dupDirTB.ReadOnly = true;
            this.dupDirTB.Size = new System.Drawing.Size(60, 23);
            this.dupDirTB.TabIndex = 28;
            // 
            // label6
            // 
            this.label6.AutoSize = true;
            this.label6.Location = new System.Drawing.Point(11, 191);
            this.label6.Name = "label6";
            this.label6.Size = new System.Drawing.Size(48, 15);
            this.label6.TabIndex = 27;
            this.label6.Text = "Images:";
            // 
            // imageCountTB
            // 
            this.imageCountTB.Location = new System.Drawing.Point(92, 188);
            this.imageCountTB.Name = "imageCountTB";
            this.imageCountTB.ReadOnly = true;
            this.imageCountTB.Size = new System.Drawing.Size(68, 23);
            this.imageCountTB.TabIndex = 26;
            // 
            // label5
            // 
            this.label5.AutoSize = true;
            this.label5.Location = new System.Drawing.Point(11, 162);
            this.label5.Name = "label5";
            this.label5.Size = new System.Drawing.Size(33, 15);
            this.label5.TabIndex = 25;
            this.label5.Text = "Files:";
            // 
            // fileCountTB
            // 
            this.fileCountTB.Location = new System.Drawing.Point(92, 159);
            this.fileCountTB.Name = "fileCountTB";
            this.fileCountTB.ReadOnly = true;
            this.fileCountTB.Size = new System.Drawing.Size(68, 23);
            this.fileCountTB.TabIndex = 24;
            // 
            // label4
            // 
            this.label4.AutoSize = true;
            this.label4.Location = new System.Drawing.Point(11, 133);
            this.label4.Name = "label4";
            this.label4.Size = new System.Drawing.Size(66, 15);
            this.label4.TabIndex = 23;
            this.label4.Text = "Direcrories:";
            // 
            // directoryCountTB
            // 
            this.directoryCountTB.Location = new System.Drawing.Point(92, 128);
            this.directoryCountTB.Name = "directoryCountTB";
            this.directoryCountTB.ReadOnly = true;
            this.directoryCountTB.Size = new System.Drawing.Size(68, 23);
            this.directoryCountTB.TabIndex = 22;
            // 
            // scannerStatus
            // 
            this.scannerStatus.Location = new System.Drawing.Point(92, 72);
            this.scannerStatus.Name = "scannerStatus";
            this.scannerStatus.Size = new System.Drawing.Size(362, 23);
            this.scannerStatus.TabIndex = 21;
            // 
            // startBtn
            // 
            this.startBtn.Enabled = false;
            this.startBtn.Location = new System.Drawing.Point(460, 23);
            this.startBtn.Name = "startBtn";
            this.startBtn.Size = new System.Drawing.Size(95, 23);
            this.startBtn.TabIndex = 20;
            this.startBtn.Text = "Start";
            this.startBtn.UseVisualStyleBackColor = true;
            this.startBtn.Click += new System.EventHandler(this.OnStart);
            // 
            // label1
            // 
            this.label1.AutoSize = true;
            this.label1.Location = new System.Drawing.Point(11, 25);
            this.label1.Name = "label1";
            this.label1.Size = new System.Drawing.Size(35, 15);
            this.label1.TabIndex = 19;
            this.label1.Text = "Scan:";
            // 
            // scanThisBtn
            // 
            this.scanThisBtn.Enabled = false;
            this.scanThisBtn.Location = new System.Drawing.Point(421, 22);
            this.scanThisBtn.Name = "scanThisBtn";
            this.scanThisBtn.Size = new System.Drawing.Size(33, 23);
            this.scanThisBtn.TabIndex = 18;
            this.scanThisBtn.Text = "...";
            this.scanThisBtn.UseVisualStyleBackColor = true;
            this.scanThisBtn.Click += new System.EventHandler(this.OnScanThis);
            // 
            // scanThis
            // 
            this.scanThis.Enabled = false;
            this.scanThis.Location = new System.Drawing.Point(92, 22);
            this.scanThis.Name = "scanThis";
            this.scanThis.Size = new System.Drawing.Size(330, 23);
            this.scanThis.TabIndex = 17;
            // 
            // curDirTB
            // 
            this.curDirTB.Location = new System.Drawing.Point(284, 130);
            this.curDirTB.Name = "curDirTB";
            this.curDirTB.ReadOnly = true;
            this.curDirTB.Size = new System.Drawing.Size(271, 23);
            this.curDirTB.TabIndex = 32;
            // 
            // curFileTB
            // 
            this.curFileTB.Location = new System.Drawing.Point(284, 159);
            this.curFileTB.Name = "curFileTB";
            this.curFileTB.ReadOnly = true;
            this.curFileTB.Size = new System.Drawing.Size(271, 23);
            this.curFileTB.TabIndex = 33;
            // 
            // InitialSelection
            // 
            this.AutoScaleDimensions = new System.Drawing.SizeF(7F, 15F);
            this.AutoScaleMode = System.Windows.Forms.AutoScaleMode.Font;
            this.ClientSize = new System.Drawing.Size(598, 635);
            this.Controls.Add(this.scanGB);
            this.Controls.Add(this.groupBox2);
            this.Controls.Add(this.groupBox1);
            this.Name = "InitialSelection";
            this.Text = "InitialSelection";
            this.groupBox1.ResumeLayout(false);
            this.groupBox2.ResumeLayout(false);
            this.groupBox2.PerformLayout();
            this.scanGB.ResumeLayout(false);
            this.scanGB.PerformLayout();
            this.ResumeLayout(false);

        }

        #endregion

        private System.Windows.Forms.Button openExistingBtn;
        private System.Windows.Forms.GroupBox groupBox1;
        private System.Windows.Forms.GroupBox groupBox2;
        private System.Windows.Forms.Button createBtn;
        private System.Windows.Forms.Button chooseTop;
        private System.Windows.Forms.Label label3;
        private System.Windows.Forms.Label label2;
        private System.Windows.Forms.Button openExisting;
        private System.Windows.Forms.TextBox setLocation;
        private System.Windows.Forms.TextBox relativeTo;
        private System.Windows.Forms.GroupBox scanGB;
        private System.Windows.Forms.Label label1;
        private System.Windows.Forms.Button scanThisBtn;
        private System.Windows.Forms.TextBox scanThis;
        private System.Windows.Forms.Button startBtn;
        private System.Windows.Forms.TextBox scannerStatus;
        private System.Windows.Forms.Timer monitorTimer;
        private System.Windows.Forms.Label label6;
        private System.Windows.Forms.TextBox imageCountTB;
        private System.Windows.Forms.Label label5;
        private System.Windows.Forms.TextBox fileCountTB;
        private System.Windows.Forms.Label label4;
        private System.Windows.Forms.TextBox directoryCountTB;
        private System.Windows.Forms.Label label8;
        private System.Windows.Forms.TextBox dupFilesTB;
        private System.Windows.Forms.Label label7;
        private System.Windows.Forms.TextBox dupDirTB;
        private System.Windows.Forms.TextBox curFileTB;
        private System.Windows.Forms.TextBox curDirTB;
    }
}