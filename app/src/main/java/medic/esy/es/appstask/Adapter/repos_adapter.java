package medic.esy.es.appstask.Adapter;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.List;

import medic.esy.es.appstask.R;
import medic.esy.es.appstask.model.item;

public class repos_adapter extends RecyclerView.Adapter<repos_adapter.ViewHolder> {
    private List<item> items;
    private Context context;
    Dialog mDialog;
    public String data;
    SharedPreferences sharedPreferences;
    public repos_adapter(Context applicationContext, List<item> itemArrayList) {
        this.context = applicationContext;
        this.items = itemArrayList;
    }

    @Override
    public repos_adapter.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.repos_row, viewGroup, false);
        final ViewHolder viewHolder=new ViewHolder(view);
        mDialog =new Dialog(view.getContext());
        mDialog.setContentView(R.layout.dialog_view);

        mDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        viewHolder.holderLayout.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {

                mDialog.show();

                return true;
            }
        });
        Button r1=(Button)mDialog.findViewById(R.id.repositoryUrl);
        Button r2=(Button)mDialog.findViewById(R.id.ownerUrl);

        r1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent myIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(sharedPreferences.getString("repository","empty")));
                mDialog.getContext().startActivity(myIntent);            }
        });

        r2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent myIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(sharedPreferences.getString("owner","empty")));
                mDialog.getContext().startActivity(myIntent);
            }
        });
;
        return new ViewHolder(view);
    }


    ///this method used to filter data in recycleView//////

    public void filterList(List<item>filtered){
        items=filtered;
        notifyDataSetChanged();
    }

    @Override
    public void onBindViewHolder(repos_adapter.ViewHolder viewHolder, int i) {

        viewHolder.repositoryName.setText(items.get(i).getName());
        viewHolder.userName.setText(items.get(i).getFullName());
        viewHolder.description.setText(items.get(i).getDescription());
        sharedPreferences  = context.getSharedPreferences("mypref",context.MODE_PRIVATE);
        SharedPreferences.Editor myeditor  =sharedPreferences.edit();
        myeditor.putString("repository",items.get(i).getClone_url());
        myeditor.putString("owner",items.get(i).getHtmlUrl());
        myeditor.commit();

        /// depend on fork change color of item //
        String forkFlag= String.valueOf(items.get(i).getFork());
        if(forkFlag.equals("false") || forkFlag.equals(" ")){
            viewHolder.itemView.setBackgroundColor(Color.GREEN);
        }else{
            viewHolder.itemView.setBackgroundColor(Color.WHITE);
        }


    }

    @Override
    public int getItemCount() {
        return items.size();
    }


    public class ViewHolder extends RecyclerView.ViewHolder {
        private TextView repositoryName, userName,description;
        private LinearLayout holderLayout;


        public ViewHolder(View view) {
            super(view);
            repositoryName = (TextView) view.findViewById(R.id.reposName);
            userName = (TextView) view.findViewById(R.id.userName);
            description = (TextView) view.findViewById(R.id.description);
            holderLayout=(LinearLayout) view.findViewById(R.id.holderLayout);

            Typeface myfont =Typeface.createFromAsset(context.getAssets(),"fonts/font2.ttf");
            repositoryName.setTypeface(myfont);
            userName.setTypeface(myfont);
        }

    }


    }


