package com.mobilez365.xo.fragments;

import android.app.Activity;
import android.app.Dialog;
import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

import com.mobilez365.xo.R;
import com.mobilez365.xo.ai.FieldValue;
import com.mobilez365.xo.util.Constant;

/**
 * Created by BruSD on 06.05.2014.
 */
public class TwoPlayerFragment extends Fragment{


    private View rootView;
    private Activity parentActivity;

    private FieldValue[][] fieldValuesMatrix;
    private FieldValue [] fieldValuesArray;
    private GridView gridview;
    private TextView myUserNameTextView, oponentUserNameTextView, mySignTextView, oponentSignTextView;
    private ImageView  myAvatarImageView, oponentAvatarImageView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_game_layout, container, false);
        parentActivity = getActivity();
        initialAllView();

        initFieldValues();

        fillDataInView();
        fillPlayersData();
        initialGameField();
        return rootView;
    }



    private void fillPlayersData() {

    }

    private void fillDataInView() {

    }

    private void initialAllView() {
        gridview = (GridView)rootView.findViewById(R.id.game_xo_grid_view);

        mySignTextView = (TextView)rootView.findViewById(R.id.user_signe_text_view_game_fragment);
        oponentSignTextView = (TextView)rootView.findViewById(R.id.oponent_signe_text_view_game_fragment);

        myUserNameTextView = (TextView)rootView.findViewById(R.id.user_name_text_view_game_fragment);
        oponentUserNameTextView = (TextView)rootView.findViewById(R.id.oponent_name_text_view_game_fragment);

        myAvatarImageView = (ImageView)rootView.findViewById(R.id.my_avatar_image_view_game_fragment);
        oponentAvatarImageView = (ImageView)rootView.findViewById(R.id.oponent_avatar_image_view_game_fragment);
    }

    private void initFieldValues(){
        fieldValuesMatrix = new FieldValue[3][3];
        fieldValuesArray = new FieldValue[9];
        for (int i =0; i < 3; i++){
            for (int j = 0; j < 3; j++){
                fieldValuesMatrix[i][j] = FieldValue.Empty;
                fieldValuesArray[i*3+j] = FieldValue.Empty;
            }
        }
    }
    private void fieldArrayToMarix(){
        for (int i =0; i < 3; i++){
            for (int j = 0; j < 3; j++){
                fieldValuesMatrix[i][j] = fieldValuesArray[i*3+j];

            }
        }
    }

    private void showWinDialog(){
        Dialog dialog = new Dialog(parentActivity);
        dialog.setTitle("You Win");
    }
    private void showLoseDialog(){
        Dialog dialog = new Dialog(parentActivity);
        dialog.setTitle("You Lose");
    }
    private void showDrawDialog(){
        Dialog dialog = new Dialog(parentActivity);
        dialog.setTitle("Drawn game");
    }

    private void initialGameField(){

        gridview.setAdapter(new XOImageAdapter(parentActivity, fieldValuesArray ));

    }
    public class XOImageAdapter extends BaseAdapter {
        private Context mContext;
        private int mySymbole;
        private FieldValue[] fieldValuesG;

        public XOImageAdapter(Context c,  FieldValue[] fieldValues) {
            mContext = c;
            this.mySymbole = mySymbole;
            this.fieldValuesG = fieldValues;
        }

        public int getCount() {
            return fieldValuesG.length;
        }

        public Object getItem(int position) {
            return null;
        }

        public long getItemId(int position) {
            return 0;
        }

        // create a new ImageView for each item referenced by the Adapter
        public View getView(final int position,  View convertView, ViewGroup parent) {



            LayoutInflater inflater = (LayoutInflater) mContext.getSystemService( Context.LAYOUT_INFLATER_SERVICE );
            convertView = inflater.inflate(R.layout.grid_view_cell_layout, parent, false);



            final ImageView imageView = (ImageView) convertView.findViewById(R.id.xo_cell_image_view);
            FieldValue id = fieldValuesG[position];
            switch (id){
                case Empty:{
                    imageView.setImageDrawable(parentActivity.getResources().getDrawable(R.drawable.zero_field));
                    break;
                }
                case X:{
                    String crossName = "cross_" + "1";
                    imageView.setImageDrawable(parentActivity.getResources().getDrawable(parentActivity.getResources().getIdentifier(crossName, "drawable", parentActivity.getPackageName())));
                    break;
                }
                case O:{
                    String zeroName = "zero_" + "1";
                    imageView.setImageDrawable(parentActivity.getResources().getDrawable(parentActivity.getResources().getIdentifier(zeroName, "drawable", parentActivity.getPackageName())));
                    break;
                }

            }
            imageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                }
            });


            return convertView;
        }

    }

}
