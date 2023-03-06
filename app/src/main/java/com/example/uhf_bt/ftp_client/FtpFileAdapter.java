package com.example.uhf_bt.ftp_client;


import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.uhf_bt.R;

import it.sauronsoftware.ftp4j.FTPFile;

/********************************************
 *     Created by DailyCoding on 18-Jan-23.  *
 ********************************************/

public class FtpFileAdapter extends RecyclerView.Adapter<FtpFileAdapter.Holder> {

    private Context context;
    private FTPFile[] ftpFiles;
    private OnItemClickListener listener;

    public FtpFileAdapter(Context context, FTPFile[] ftpFiles) {
        this.context = context;
        this.ftpFiles = ftpFiles;
    }

    public void setListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    public void setFtpFiles(FTPFile[] ftpFiles) {
        this.ftpFiles = ftpFiles;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new Holder(LayoutInflater.from(context).inflate(R.layout.item_ftp_file, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull Holder holder, int position) {
        holder.txt_file_name.setText(ftpFiles[position].getName());


        if (ftpFiles[position].getType() == 1) {
            //FOLDER
            holder.img_icon.setImageResource(R.drawable.folder);
            holder.txt_file_type.setText("Folder");
        } else if (ftpFiles[position].getType() == 0) {
            //FILE
            holder.img_icon.setImageResource(R.drawable.ic_text_file);

            //SET EXTENSION
            if (ftpFiles[position].getName().contains(".")) {
                String extension = ftpFiles[position].getName().substring(ftpFiles[position].getName().lastIndexOf("."));
                holder.txt_file_type.setText(extension);
            } else {
                holder.txt_file_type.setText("File");
            }
        }

        holder.root.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (ftpFiles[position].getType() == 1) {
                    //FOLDER
                    listener.onFolderClicked(ftpFiles[position].getName());
                } else if (ftpFiles[position].getType() == 0) {
                    //FILE
                    listener.onFileClicked(ftpFiles[position].getName());
                }
            }
        });

    }

    @Override
    public int getItemCount() {
        return ftpFiles != null ? ftpFiles.length : 0;
    }

    public class Holder extends RecyclerView.ViewHolder {
        private TextView txt_file_name, txt_file_type;
        private ImageView img_icon;
        private RelativeLayout root;

        public Holder(@NonNull View itemView) {
            super(itemView);
            txt_file_name = itemView.findViewById(R.id.txt_file_name);
            txt_file_type = itemView.findViewById(R.id.txt_file_type);
            img_icon = itemView.findViewById(R.id.img_icon);
            root = itemView.findViewById(R.id.root);
        }
    }

    public interface OnItemClickListener{
        void onFolderClicked(String name);
        void onFileClicked(String name);
    }
}
