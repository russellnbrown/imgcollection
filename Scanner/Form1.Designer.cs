
namespace Scanner
{
    partial class Form1
    {
        /// <summary>
        ///  Required designer variable.
        /// </summary>
        private System.ComponentModel.IContainer components = null;

        /// <summary>
        ///  Clean up any resources being used.
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
        ///  Required method for Designer support - do not modify
        ///  the contents of this method with the code editor.
        /// </summary>
        private void InitializeComponent()
        {
            this.relativeTo = new System.Windows.Forms.TextBox();
            this.setLocation = new System.Windows.Forms.TextBox();
            this.scanLocation = new System.Windows.Forms.TextBox();
            this.button1 = new System.Windows.Forms.Button();
            this.openExisting = new System.Windows.Forms.Button();
            this.label1 = new System.Windows.Forms.Label();
            this.createNew = new System.Windows.Forms.Button();
            this.label2 = new System.Windows.Forms.Label();
            this.label3 = new System.Windows.Forms.Label();
            this.chooseTop = new System.Windows.Forms.Button();
            this.chooseScanLocation = new System.Windows.Forms.Button();
            this.statusText = new System.Windows.Forms.TextBox();
            this.SuspendLayout();
            // 
            // relativeTo
            // 
            this.relativeTo.Location = new System.Drawing.Point(106, 22);
            this.relativeTo.Name = "relativeTo";
            this.relativeTo.Size = new System.Drawing.Size(362, 23);
            this.relativeTo.TabIndex = 0;
            // 
            // setLocation
            // 
            this.setLocation.Location = new System.Drawing.Point(106, 69);
            this.setLocation.Name = "setLocation";
            this.setLocation.Size = new System.Drawing.Size(362, 23);
            this.setLocation.TabIndex = 1;
            // 
            // scanLocation
            // 
            this.scanLocation.Location = new System.Drawing.Point(106, 117);
            this.scanLocation.Name = "scanLocation";
            this.scanLocation.Size = new System.Drawing.Size(362, 23);
            this.scanLocation.TabIndex = 2;
            // 
            // button1
            // 
            this.button1.Location = new System.Drawing.Point(56, 326);
            this.button1.Name = "button1";
            this.button1.Size = new System.Drawing.Size(95, 23);
            this.button1.TabIndex = 3;
            this.button1.Text = "Scan";
            this.button1.UseVisualStyleBackColor = true;
            this.button1.Click += new System.EventHandler(this.OnScan);
            // 
            // openExisting
            // 
            this.openExisting.Location = new System.Drawing.Point(474, 68);
            this.openExisting.Name = "openExisting";
            this.openExisting.Size = new System.Drawing.Size(95, 23);
            this.openExisting.TabIndex = 4;
            this.openExisting.Text = "Open Existing";
            this.openExisting.UseVisualStyleBackColor = true;
            this.openExisting.Click += new System.EventHandler(this.openExisting_Click);
            // 
            // label1
            // 
            this.label1.AutoSize = true;
            this.label1.Location = new System.Drawing.Point(25, 120);
            this.label1.Name = "label1";
            this.label1.Size = new System.Drawing.Size(57, 15);
            this.label1.TabIndex = 5;
            this.label1.Text = "Scan Top:";
            // 
            // createNew
            // 
            this.createNew.Location = new System.Drawing.Point(575, 68);
            this.createNew.Name = "createNew";
            this.createNew.Size = new System.Drawing.Size(95, 23);
            this.createNew.TabIndex = 6;
            this.createNew.Text = "Create New";
            this.createNew.UseVisualStyleBackColor = true;
            this.createNew.Click += new System.EventHandler(this.OnCreateNewScan);
            // 
            // label2
            // 
            this.label2.AutoSize = true;
            this.label2.Location = new System.Drawing.Point(25, 72);
            this.label2.Name = "label2";
            this.label2.Size = new System.Drawing.Size(75, 15);
            this.label2.TabIndex = 7;
            this.label2.Text = "Set Location:";
            // 
            // label3
            // 
            this.label3.AutoSize = true;
            this.label3.Location = new System.Drawing.Point(25, 25);
            this.label3.Name = "label3";
            this.label3.Size = new System.Drawing.Size(66, 15);
            this.label3.TabIndex = 8;
            this.label3.Text = "Relative To:";
            // 
            // chooseTop
            // 
            this.chooseTop.Location = new System.Drawing.Point(474, 22);
            this.chooseTop.Name = "chooseTop";
            this.chooseTop.Size = new System.Drawing.Size(95, 23);
            this.chooseTop.TabIndex = 9;
            this.chooseTop.Text = "Choose";
            this.chooseTop.UseVisualStyleBackColor = true;
            this.chooseTop.Click += new System.EventHandler(this.OnChooseRelativeTo);
            // 
            // chooseScanLocation
            // 
            this.chooseScanLocation.Location = new System.Drawing.Point(474, 116);
            this.chooseScanLocation.Name = "chooseScanLocation";
            this.chooseScanLocation.Size = new System.Drawing.Size(95, 23);
            this.chooseScanLocation.TabIndex = 10;
            this.chooseScanLocation.Text = "Coose Scan Location";
            this.chooseScanLocation.UseVisualStyleBackColor = true;
            this.chooseScanLocation.Click += new System.EventHandler(this.OnChooseScanLocation);
            // 
            // statusText
            // 
            this.statusText.Location = new System.Drawing.Point(106, 158);
            this.statusText.Name = "statusText";
            this.statusText.Size = new System.Drawing.Size(362, 23);
            this.statusText.TabIndex = 11;
            // 
            // Form1
            // 
            this.AutoScaleDimensions = new System.Drawing.SizeF(7F, 15F);
            this.AutoScaleMode = System.Windows.Forms.AutoScaleMode.Font;
            this.ClientSize = new System.Drawing.Size(800, 450);
            this.Controls.Add(this.statusText);
            this.Controls.Add(this.chooseScanLocation);
            this.Controls.Add(this.chooseTop);
            this.Controls.Add(this.label3);
            this.Controls.Add(this.label2);
            this.Controls.Add(this.createNew);
            this.Controls.Add(this.label1);
            this.Controls.Add(this.openExisting);
            this.Controls.Add(this.button1);
            this.Controls.Add(this.scanLocation);
            this.Controls.Add(this.setLocation);
            this.Controls.Add(this.relativeTo);
            this.Name = "Form1";
            this.Text = "Form1";
            this.ResumeLayout(false);
            this.PerformLayout();

        }

        #endregion

        private System.Windows.Forms.TextBox relativeTo;
        private System.Windows.Forms.TextBox setLocation;
        private System.Windows.Forms.TextBox scanLocation;
        private System.Windows.Forms.Button button1;
        private System.Windows.Forms.Button openExisting;
        private System.Windows.Forms.Label label1;
        private System.Windows.Forms.Button createNew;
        private System.Windows.Forms.Label label2;
        private System.Windows.Forms.Label label3;
        private System.Windows.Forms.Button chooseTop;
        private System.Windows.Forms.Button chooseScanLocation;
        private System.Windows.Forms.TextBox statusText;
    }
}

