namespace csview
{
    partial class CSViewForm
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
            this.cslist = new System.Windows.Forms.ListView();
            this.imglist = new System.Windows.Forms.ImageList(this.components);
            this.SuspendLayout();
            // 
            // cslist
            // 
            this.cslist.Dock = System.Windows.Forms.DockStyle.Fill;
            this.cslist.Location = new System.Drawing.Point(0, 0);
            this.cslist.Margin = new System.Windows.Forms.Padding(1);
            this.cslist.Name = "cslist";
            this.cslist.Size = new System.Drawing.Size(800, 450);
            this.cslist.TabIndex = 0;
            this.cslist.UseCompatibleStateImageBehavior = false;
            this.cslist.SelectedIndexChanged += new System.EventHandler(this.cslist_SelectedIndexChanged);
            // 
            // imglist
            // 
            this.imglist.ColorDepth = System.Windows.Forms.ColorDepth.Depth24Bit;
            this.imglist.ImageSize = new System.Drawing.Size(32, 32);
            this.imglist.TransparentColor = System.Drawing.Color.Transparent;
            // 
            // CSViewForm
            // 
            this.AutoScaleDimensions = new System.Drawing.SizeF(6F, 13F);
            this.AutoScaleMode = System.Windows.Forms.AutoScaleMode.Font;
            this.ClientSize = new System.Drawing.Size(800, 450);
            this.Controls.Add(this.cslist);
            this.Name = "CSViewForm";
            this.Text = "CSView";
            this.Load += new System.EventHandler(this.CSViewForm_Load);
            this.ResumeLayout(false);

        }

        #endregion

        private System.Windows.Forms.ListView cslist;
        private System.Windows.Forms.ImageList imglist;
    }
}

