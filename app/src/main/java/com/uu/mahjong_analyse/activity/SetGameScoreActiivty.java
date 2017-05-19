package com.uu.mahjong_analyse.activity;

import android.app.Dialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.support.design.widget.TextInputEditText;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.text.format.DateFormat;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.uu.mahjong_analyse.R;
import com.uu.mahjong_analyse.Utils.CommonApi;
import com.uu.mahjong_analyse.Utils.MyApplication;
import com.uu.mahjong_analyse.base.BaseActivity;
import com.uu.mahjong_analyse.bean.PlayerRecord;
import com.uu.mahjong_analyse.db.DBDao;

import java.util.Arrays;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * @auther Nagisa.
 * @date 2016/7/2.
 */
public class SetGameScoreActiivty extends BaseActivity {

    @BindView(R.id.et_east)
    TextInputEditText mEtEast;
    @BindView(R.id.et_south)
    TextInputEditText mEtSouth;
    @BindView(R.id.et_west)
    TextInputEditText mEtWest;
    @BindView(R.id.et_north)
    TextInputEditText mEtNorth;
    @BindView(R.id.toolbar)
    Toolbar mToolbar;
    @BindView(R.id.btn_save)
    Button mBtnSave;

    private int topBonus = 20;
    private PlayerRecord mNorthPlayer;
    private PlayerRecord mSouthPlayer;
    private PlayerRecord mWestPlayer;
    private PlayerRecord mEastPlayer;
    private String[] names = new String[4];
    private int mSum;

    @Override
    public void initData() {
        mEastPlayer = DBDao.selectPlayer(MyApplication.param.get("east"));
        mWestPlayer = DBDao.selectPlayer(MyApplication.param.get("west"));
        mSouthPlayer = DBDao.selectPlayer(MyApplication.param.get("south"));
        mNorthPlayer = DBDao.selectPlayer(MyApplication.param.get("north"));


        names[0] = MyApplication.param.get("east");
        names[1] = MyApplication.param.get("west");
        names[2] = MyApplication.param.get("south");
        names[3] = MyApplication.param.get("north");

    }

    @Override
    public void initView() {
        setContentView(R.layout.activity_set_game_score);
        ButterKnife.bind(this);

        CommonApi.setToolbar(this, mToolbar, "设置全局得点");
    }

    @Override
    public void initEvent() {

    }


    @OnClick(R.id.btn_save)
    public void onClick() {
        String east = mEtEast.getText().toString().trim();
        String west = mEtWest.getText().toString().trim();
        String north = mEtNorth.getText().toString().trim();
        String south = mEtSouth.getText().toString().trim();

        if(TextUtils.isEmpty(east) || TextUtils.isEmpty(west) || TextUtils.isEmpty(south) || TextUtils.isEmpty(north)) {
            Toast.makeText(this, "4个人都填了再保存啊喂！", Toast.LENGTH_SHORT).show();
        } else {
//            ",date text" +       //日期
//                    ",top text" +        //名字 + 得点
//                    ",second text" +        //名字 + 得点
//                    ",third text" +        //名字 + 得点
//                    ",last text" +
            Float east_d = (Float.parseFloat(east) - 30000) / 1000;
            Float west_d = (Float.parseFloat(west) - 30000) / 1000;
            Float south_d = (Float.parseFloat(south) - 30000) / 1000;
            Float north_d = (Float.parseFloat(north) - 30000) / 1000;


            int[] arr = new int[4];
            arr[0] = Math.round(east_d);
            arr[1] = Math.round(west_d);
            arr[2] = Math.round(south_d);
            arr[3] = Math.round(north_d);

            mSum = 0;
            for(int i = 0; i < 4; i++) {
                mSum += arr[i];
            }


            mEastPlayer.score = Math.round(east_d);
            mWestPlayer.score = Math.round(west_d);
            mSouthPlayer.score = Math.round(south_d);
            mNorthPlayer.score = Math.round(north_d);



            TreeSet<PlayerRecord> ts = new TreeSet<>(new Comparator<PlayerRecord>() {
                @Override
                public int compare(PlayerRecord lhs, PlayerRecord rhs) {
                    return lhs.score-rhs.score==0?1:lhs.score-rhs.score;
                }
            });
            ts.add(mEastPlayer);
            ts.add(mWestPlayer);
            ts.add(mSouthPlayer);
            ts.add(mNorthPlayer);

            Iterator<PlayerRecord> iterator = ts.iterator();

            ContentValues cv = new ContentValues();

            View dialog_view = View.inflate(this, R.layout.game_score, null);
            TextView tvTop = (TextView) dialog_view.findViewById(R.id.tv_top);
            TextView tvSecond = (TextView) dialog_view.findViewById(R.id.tv_second);
            TextView tvThird = (TextView) dialog_view.findViewById(R.id.tv_third);
            TextView tvLast = (TextView) dialog_view.findViewById(R.id.tv_last);

            saveData(names, iterator, cv,"last", tvLast);
            saveData(names, iterator, cv,"third", tvThird);
            saveData(names, iterator, cv,"second", tvSecond);
            saveData(names, iterator, cv,"top", tvTop);

            cv.put("date", (String) DateFormat.format("yyyy-MM-dd HH:mm:ss", System.currentTimeMillis()));
            DBDao.insertGame(cv);
            Toast.makeText(this, "全局得点保存成功", Toast.LENGTH_SHORT).show();

            new AlertDialog.Builder(this).setView(dialog_view).setTitle("战绩").setPositiveButton("确定", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                    finish();
                }
            }).show();
        }

    }

    private void setTopScore(int max, int sum,PlayerRecord pr) {
        if(pr.score == max) {
            pr.score -= sum; //sum一般情况下都是-20，如果有误差，那么抹平
        }
    }

    private void saveData(String[] names, Iterator<PlayerRecord> iterator, ContentValues cv,String shunwei, TextView tv) {
        if (iterator.hasNext()) {
            PlayerRecord player = iterator.next();
            for(String name : names) {
                if(TextUtils.equals(name, player.name)) {

                    ContentValues cv_player = new ContentValues();
                    switch (shunwei) {
                        case "last":
                            cv_player.put(shunwei,player.last+1);
                            break;
                        case "third":
                            cv_player.put(shunwei,player.third+1);
                            break;
                        case "second":
                            cv_player.put(shunwei,player.second+1);
                            break;
                        case "top":
                            cv_player.put(shunwei,player.top+1);
                            break;
                    }

                    if(TextUtils.equals(shunwei, "top")) {
                        player.score -= mSum;       //sum一般情况下应该是-20，这里就是加上了头名赏，如果有误差则抹平了
                    }
                    String result = player.name + ": " + player.score;
                    cv.put(shunwei,result);
                    cv_player.put("total_games", player.total_games + 1);
                    cv_player.put("score_sum", player.score_sum + player.score );
                    tv.setText(result);
                    DBDao.updatePlayerData(player.name, cv_player);
                }
            }
        }
    }




    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                break;
        }
        return super.onOptionsItemSelected(item);
    }


}