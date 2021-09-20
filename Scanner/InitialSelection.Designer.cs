
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
            this.label1 = new System.Windows.Forms.Label();
            this.scanThisBtn = new System.Windows.Forms.Button();
            this.scanThis = new System.Windows.Forms.TextBox();
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
            this.groupBox2.Size = new System.Drawing.Size(573, 155);
            this.groupBox2.TabIndex = 2;
            this.groupBox2.TabStop = false;
            this.groupBox2.Text = "Create New";
            // 
            // chooseTop
            // 
            this.chooseTop.Location = new System.Drawing.Point(460, 22);
            this.chooseTop.Name = "chooseTop";
            this.chooseTop.Size = new System.Drawing.Size(95, 23);
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
            this.openExisting.Location = new System.Drawing.Point(460, 68);
            this.openExisting.Name = "openExisting";
            this.openExisting.Size = new System.Drawing.Size(95, 23);
            this.openExisting.TabIndex = 14;
            this.openExisting.Text = "...";
            this.openExisting.UseVisualStyleBackColor = true;
            this.openExisting.Click += new System.EventHandler(this.OnSetSetLocation);
            // 
            // setLocation
            // 
            this.setLocation.Location = new System.Drawing.Point(92, 69);
            this.setLocation.Name = "setLocation";
            this.setLocation.Size = new System.Drawing.Size(362, 23);
            this.setLocation.TabIndex = 12;
            // 
            // relativeTo
            // 
            this.relativeTo.Location = new System.Drawing.Point(92, 22);
            this.relativeTo.Name = "relativeTo";
            this.relativeTo.Size = new System.Drawing.Size(362, 23);
            this.relativeTo.TabIndex = 11;
            this.relativeTo.Click += new System.EventHandler(this.OnSetRelativeTo);
            // 
            // createBtn
            // 
            this.createBtn.Location = new System.Drawing.Point(460, 118);
            this.createBtn.Name = "createBtn";
            this.createBtn.Size = new System.Drawing.Size(95, 23);
            this.createBtn.TabIndex = 0;
            this.createBtn.Text = "Create";
            this.createBtn.UseVisualStyleBackColor = true;
            this.createBtn.Click += new System.EventHandler(this.OnCreateNew);
            // 
            // scanGB
            // 
            this.scanGB.Controls.Add(this.label1);
            this.scanGB.Controls.Add(this.scanThisBtn);
            this.scanGB.Controls.Add(this.scanThis);
            this.scanGB.Location = new System.Drawing.Point(12, 273);
            this.scanGB.Name = "scanGB";
            this.scanGB.Size = new System.Drawing.Size(574, 350);
            this.scanGB.TabIndex = 3;
            this.scanGB.TabStop = false;
            this.scanGB.Text = "Scanner";
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
            this.scanThisBtn.Location = new System.Drawing.Point(460, 21);
            this.scanThisBtn.Name = "scanThisBtn";
            this.scanThisBtn.Size = new System.Drawing.Size(95, 23);
            this.scanThisBtn.TabIndex = 18;
            this.scanThisBtn.Text = "...";
            this.scanThisBtn.UseVisualStyleBackColor = true;
            this.scanThisBtn.Click += new System.EventHandler(this.OnScanThis);
            // 
            // scanThis
            // 
            this.scanThis.Location = new System.Drawing.Point(92, 22);
            this.scanThis.Name = "scanThis";
            this.scanThis.Size = new System.Drawing.Size(362, 23);
            this.scanThis.TabIndex = 17;
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
    }
}