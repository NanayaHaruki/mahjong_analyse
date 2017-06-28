package com.uu.mahjong_analyse.activity;

import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.SparseArray;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.getbase.floatingactionbutton.FloatingActionsMenu;
import com.romainpiel.shimmer.Shimmer;
import com.romainpiel.shimmer.ShimmerTextView;
import com.uu.mahjong_analyse.R;
import com.uu.mahjong_analyse.Utils.Constant;
import com.uu.mahjong_analyse.Utils.SPUtils;
import com.uu.mahjong_analyse.Utils.ToastUtils;
import com.uu.mahjong_analyse.Utils.rx.RxBus;
import com.uu.mahjong_analyse.base.BaseActivity;
import com.uu.mahjong_analyse.bean.LiujuResult;
import com.uu.mahjong_analyse.db.DBDao;
import com.uu.mahjong_analyse.fragment.LeftMenuFragment;
import com.uu.mahjong_analyse.view.LiuJuDialog;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import rx.functions.Action1;

public class MainActivity extends BaseActivity {
    private static final int RC_PLAYERS = 1;
    private static final int RC_GET_SCORE = 2;
    private static final int RC_ALBUM = 3;

    @BindView(R.id.fab_menu)
    FloatingActionsMenu mFabMenu;
    @BindView(R.id.toolbar)
    Toolbar mToolbar;
    @BindView(R.id.drawerlayout)
    DrawerLayout mDrawerLayout;

    ActionBarDrawerToggle mDrawerToggle;
    @BindView(R.id.tv_east)
    ShimmerTextView mTvEast;
    @BindView(R.id.tv_south)
    ShimmerTextView mTvSouth;
    @BindView(R.id.tv_west)
    ShimmerTextView mTvWest;
    @BindView(R.id.tv_north)
    ShimmerTextView mTvNorth;
    @BindView(R.id.rl_table)
    RelativeLayout mRlTable;
    @BindView(R.id.rightmenu_rg)
    RadioGroup rightmenu_radiogroup;
    @BindView(R.id.tv_east_point)
    TextView mTvEastPoint;
    @BindView(R.id.tv_south_point)
    TextView mTvSouthPoint;
    @BindView(R.id.tv_west_point)
    TextView mTvWestPoint;
    @BindView(R.id.tv_north_point)
    TextView mTvNorthPoint;
    @BindView(R.id.ll_south)
    LinearLayout mLLSouth;
    @BindView(R.id.ll_north)
    LinearLayout mLLNorth;
    @BindView(R.id.ll_west)
    LinearLayout mLLWest;
    @BindView(R.id.fab_select_player)
    com.getbase.floatingactionbutton.FloatingActionButton mFabSelectPlayer;
    @BindView(R.id.fab_start)
    com.getbase.floatingactionbutton.FloatingActionButton mFabStart;
    @BindView(R.id.tv_chang)
    TextView mTvChang;
    @BindView(R.id.tv_gong)
    TextView mTvGong;        //供托

    private String[] mPlayers;
    private LeftMenuFragment mLeftMenuFragment;
    private Shimmer mShimmer;
    private String hePlayer;
    private ArrayList<ShimmerTextView> textViews;

    @Override
    public void initData() {
        rotatePlayerName();
        mShimmer = new Shimmer();
        changMap.append(0, getString(R.string.east1));
        changMap.append(1, getString(R.string.east2));
        changMap.append(2, getString(R.string.east3));
        changMap.append(3, getString(R.string.east4));
        changMap.append(4, getString(R.string.south1));
        changMap.append(5, getString(R.string.south2));
        changMap.append(6, getString(R.string.south3));
        changMap.append(7, getString(R.string.south4));
        textViews = new ArrayList<>();
        textViews.add(mTvEast);
        textViews.add(mTvSouth);
        textViews.add(mTvWest);
        textViews.add(mTvNorth);

    }

    private void rotatePlayerName() {
        RotateAnimation northAnim = new RotateAnimation(0F, 90F, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        northAnim.setFillAfter(true);
        northAnim.setDuration(500L);
        mLLNorth.setAnimation(northAnim);
        northAnim.start();

        RotateAnimation southAnim = new RotateAnimation(0F, -90F, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        southAnim.setFillAfter(true);
        southAnim.setDuration(500L);
        mLLSouth.setAnimation(southAnim);
        southAnim.start();

        RotateAnimation westAnim = new RotateAnimation(0F, 180F, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        westAnim.setFillAfter(true);
        westAnim.setDuration(500L);
        mLLWest.setAnimation(westAnim);
        westAnim.start();

    }

    @Override
    public void initView() {
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        setSupportActionBar(mToolbar);
        setTitle("麻将统计分析工具");
        mToolbar.setPopupTheme(R.style.PopupMenu);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout, mToolbar, R.string.drawer_open,
                R.string.drawer_close);
        mDrawerToggle.syncState();
        mDrawerLayout.addDrawerListener(mDrawerToggle);

        mLeftMenuFragment = new LeftMenuFragment();
        getSupportFragmentManager().beginTransaction().add(R.id.fl_leftmenu, mLeftMenuFragment)
                .commitAllowingStateLoss();
    }


    private int chang = 0;  //0-7 代表东1到南4；
    private int gong = 0;  //流局产生的供托,有人和牌则清零
    private SparseArray<String> changMap = new SparseArray<>();
    private HashMap<String, TextView> playerTvMap = new HashMap<>();

    public void initEvent() {
//
        //流局逻辑全在这里
        RxBus.getInstance().toObservable(LiujuResult.class, Constant.RX_LIUJU_RESULT)
                .subscribe(new Action1<LiujuResult>() {
                    @Override
                    public void call(LiujuResult liujuResult) {
                        //处理流局立直的数据
                        for (String richiPlayer : liujuResult.richiPlayers) {
                            TextView tv = playerTvMap.get(richiPlayer);
                            int score = SPUtils.getInt(richiPlayer, Integer.MIN_VALUE);
                            tv.setText(String.valueOf(score - 1000));
                            SPUtils.putInt(richiPlayer, score - 1000);
                            gong += 1000;
                        }
                        mTvGong.setText(String.valueOf(gong));
                        //处理流局听牌的数据
                        //如果庄家没听牌，那么进行下一场
                        if (!liujuResult.tingpaiPlayers.contains(getOyaName())) {
                            nextChang();
                        }
                        Set<String> players = playerTvMap.keySet();
                        switch (liujuResult.tingpaiPlayers.size()) {
                            case 1:
                                for (String player : players) {
                                    TextView tv = playerTvMap.get(player);
                                    if (TextUtils.equals(player, liujuResult.tingpaiPlayers.get(0))) {
                                        int score = SPUtils.getInt(player, Integer.MIN_VALUE);
                                        tv.setText(String.valueOf(score + 3000));
                                        SPUtils.putInt(player, score + 3000);
                                    } else {
                                        int score = SPUtils.getInt(player, Integer.MIN_VALUE);
                                        tv.setText(String.valueOf(score - 1000));
                                        SPUtils.putInt(player, score - 1000);
                                    }
                                }

                                break;
                            case 2:
                                for (String player : players) {
                                    TextView tv = playerTvMap.get(player);
                                    if (liujuResult.tingpaiPlayers.contains(player)) {
                                        int score = SPUtils.getInt(player, Integer.MIN_VALUE);
                                        tv.setText(String.valueOf(score + 1500));
                                        SPUtils.putInt(player, score + 1500);
                                    } else {
                                        int score = SPUtils.getInt(player, Integer.MIN_VALUE);
                                        tv.setText(String.valueOf(score - 1500));
                                        SPUtils.putInt(player, score - 1500);
                                    }
                                }
                                break;
                            case 3:
                                for (String player : players) {
                                    TextView tv = playerTvMap.get(player);
                                    if (!liujuResult.tingpaiPlayers.contains(player)) {
                                        int score = SPUtils.getInt(player, Integer.MIN_VALUE);
                                        tv.setText(String.valueOf(score - 3000));
                                        SPUtils.putInt(player, score - 3000);
                                    } else {
                                        int score = SPUtils.getInt(player, Integer.MIN_VALUE);
                                        tv.setText(String.valueOf(score + 1000));
                                        SPUtils.putInt(player, score + 1000);
                                    }
                                }
                                break;
                            default:   //剩下的是0和4的情况，即所有人都听 或所有人都不听，那么不处理
                                break;
                        }
                    }
                });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_PLAYERS && resultCode == RESULT_OK && data != null) {
            mPlayers = (String[]) data.getCharSequenceArrayExtra("players");
            //把对局的四个人名字传过来，如果数据库中没有，就为他们建表,并将所有值变成0；
            for (int i = 0; i < mPlayers.length; i++) {
                if (DBDao.selectPlayer(mPlayers[i]) == null) {
                    DBDao.insertPlayer(Constant.Table.TABLE_PLAYER_RECORD, mPlayers[i]);
                }
            }
            mTvEast.setText(mPlayers[0]);
            mTvSouth.setText(mPlayers[1]);
            mTvWest.setText(mPlayers[2]);
            mTvNorth.setText(mPlayers[3]);
            playerTvMap.put(mPlayers[0], mTvEastPoint);
            playerTvMap.put(mPlayers[1], mTvSouthPoint);
            playerTvMap.put(mPlayers[2], mTvWestPoint);
            playerTvMap.put(mPlayers[3], mTvNorthPoint);

            mLeftMenuFragment.mLeftmenuDatas.clear();
            mLeftMenuFragment.mLeftmenuDatas.add("东家：" + SPUtils.getString(Constant.EAST, ""));
            mLeftMenuFragment.mLeftmenuDatas.add("西家：" + SPUtils.getString(Constant.WEST, ""));
            mLeftMenuFragment.mLeftmenuDatas.add("南家：" + SPUtils.getString(Constant.SOUTH, ""));
            mLeftMenuFragment.mLeftmenuDatas.add("北家：" + SPUtils.getString(Constant.NORTH, ""));
            mLeftMenuFragment.mLeftMenuAdapter.notifyDataSetChanged();

            startGame();
        } else if (requestCode == RC_GET_SCORE && resultCode == RESULT_OK) {
            //设置完分数，不是庄家则chang+1
            if (!TextUtils.equals(hePlayer, getOyaName())) {
                //改变场风和庄家闪光效果
                nextChang();
            }
            //供托要清零
            gong = 0;
            mTvGong.setText(null);
            //改变四家分数
            for (String player : mPlayers) {
                playerTvMap.get(player).setText(String.valueOf(SPUtils.getInt(player, Integer.MIN_VALUE)));
            }
        } else if (requestCode == RC_ALBUM && resultCode == RESULT_OK && data != null) {
            Uri selectedImage = data.getData();
            if (selectedImage != null) {
                String[] filePathColumn = {MediaStore.Images.Media.DATA};
                // 获取选择照片的数据视图
                Cursor cursor = getContentResolver().query(selectedImage,filePathColumn, null, null, null);
                cursor.moveToFirst();
                // 从数据视图中获取已选择图片的路径
                int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
                String picturePath = cursor.getString(columnIndex);
                cursor.close();
                // 将图片显示到界面上
                Bitmap bm = BitmapFactory.decodeFile(picturePath);
                findViewById(R.id.rl_table).setBackground(new BitmapDrawable(bm));
            }
        }
    }

    /**
     * 庄家下庄，进行下一场了
     */
    private void nextChang() {
        mTvChang.setText(changMap.get(++chang));
        mShimmer.cancel();
        mShimmer.start(getOyaTextView());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.

        //noinspection SimplifiableIfStatement
        switch (item.getItemId()) {
            case R.id.toolbar_game_record:
                openPage(true, -1, GameRecordActivity.class);
                return true;
            case R.id.toolbar_persional_record:
                openPage(true, -1, PlayerInfoActivity.class);
                break;
            //直接修改数据库，暂不提供该功能
//            case R.id.toolbar_modify_record:
//                openPage(true, -1, ModifyDbActivity.class);
//                break;
            case R.id.toolbar_change_desk:
                //调用相册
                Intent intent = new Intent(Intent.ACTION_PICK,
                        android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(intent, RC_ALBUM);
                break;
            case R.id.toolbar_liuju:
                //开始对局了才可以点击流局
                if (isStart) {
                    if (mLiujuDialog == null) {
                        mLiujuDialog = new LiuJuDialog(mContext, mPlayers);
                    }
                    mLiujuDialog.show();
                } else {
                    ToastUtils.show(mContext, "对局未开始");
                }

                break;

        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }


    private boolean isStart = false;
    private LiuJuDialog mLiujuDialog;

    @OnClick({R.id.ll_east, R.id.ll_south, R.id.ll_west, R.id.ll_north, R.id.fab_select_player, R.id.fab_start})
    public void onClick(View view) {
        Intent intent = new Intent(this, GetScoreActivity.class);
//        final String east = SPUtils.getString(Constant.EAST, "");
//        final String south = SPUtils.getString(Constant.SOUTH, "");
//        final String north = SPUtils.getString(Constant.NORTH, "");
//        final String west = SPUtils.getString(Constant.WEST, "");
        switch (view.getId()) {
            case R.id.ll_east:
                openScorePage(intent, mPlayers == null ? null : mPlayers[0]);
                break;
            case R.id.ll_south:
                openScorePage(intent, mPlayers == null ? null : mPlayers[1]);
                break;
            case R.id.ll_west:
                openScorePage(intent, mPlayers == null ? null : mPlayers[2]);
                break;
            case R.id.ll_north:
                openScorePage(intent, mPlayers == null ? null : mPlayers[3]);
                break;

            case R.id.fab_select_player:
                openPage(true, RC_PLAYERS, AddNewGameActivity.class);
                mFabMenu.collapse();
                break;
            case R.id.fab_start:
                mFabMenu.collapse();
                if (mPlayers == null) {
                    Toast.makeText(this, "还没选人呢", Toast.LENGTH_SHORT).show();
                    return;
                }
                startGame();
                break;

        }
    }

    public void startGame() {
        if (!isStart) {
            mFabStart.setTitle("对战中...");
            mFabStart.setIcon(R.drawable.stop);
            isStart = true;
            //开局就将场次变成东一局
            SPUtils.putInt(Constant.CHANG, 1);
            mTvChang.setText(changMap.get(0));
            //东家闪光，右边栏东一变色
            mShimmer.start(mTvEast);
            rightmenu_radiogroup.check(R.id.rb_east1);

            //将所有人分数初始化为25000
            initScore();
        } else {
            openPage(true, -1, SetGameScoreActiivty.class);
            mFabStart.setTitle("开局");
            mFabStart.setIcon(R.mipmap.start_game);
            isStart = false;
        }
    }

    private void initScore() {
        mTvEastPoint.setText(getString(R.string._25000));
        mTvSouthPoint.setText(getString(R.string._25000));
        mTvWestPoint.setText(getString(R.string._25000));
        mTvNorthPoint.setText(getString(R.string._25000));
        SPUtils.putInt(mPlayers[0], 25000);
        SPUtils.putInt(mPlayers[1], 25000);
        SPUtils.putInt(mPlayers[2], 25000);
        SPUtils.putInt(mPlayers[3], 25000);
    }

    private void openScorePage(Intent intent, String player) {
        if (TextUtils.isEmpty(player)) {
            Toast.makeText(this, "人都还没选呢，点个JJ", Toast.LENGTH_SHORT).show();
            return;
        }
        hePlayer = player;
        intent.putExtra("player", player);
        intent.putExtra("oya", getOyaName());
        if (isStart) {
            openPage(true, RC_GET_SCORE, intent);
        }
    }

    public void showDialog(View view) {
        switch (view.getId()) {
            case R.id.tv_east:
                String eastName = SPUtils.getString(Constant.EAST, "");
                break;
            case R.id.tv_south:
                String southName = SPUtils.getString(Constant.SOUTH, "");
                break;
            case R.id.tv_west:
                String westName = SPUtils.getString(Constant.WEST, "");
                break;
            case R.id.tv_north:
                String northName = SPUtils.getString(Constant.NORTH, "");
                break;

        }
    }

    public void closeDrawerLayout() {
        mDrawerLayout.closeDrawers();
    }

    /**
     * 获取庄家名字
     */
    private String getOyaName() {
        return mPlayers[chang % 4];
    }

    private ShimmerTextView getOyaTextView() {
        return textViews.get(chang % 4);
    }
}
