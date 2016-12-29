package com.maleant.pickerlib.libcore;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.maleant.pickerlib.libcore.util.FileUtils;
import com.yalantis.ucrop.UCrop;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import com.maleant.pickerlib.libcore.base.HImageLoader;

/**
 * Created by maleant on 2016/9/14.
 *
 */
public class GridImageActivity extends Activity {

    public static final int RESULT_CROP = 1001;
    public static final int RESULT_ERROR = 1000;

    public static final String EXTRA_PATH = "path";
    //保存拍照缓存的Uri和图片名称
    private Uri mTakePhotoUri;
    private String mTackPhotoName;

    protected static String mPhotoTargetFolder;
    boolean isDirShowing = false;
    private View view_layer;
    private TextView tv_title;
    private RecyclerView recycler_image;
    private RecyclerView recycler_dir;
    private ImageAdapter imageAdapter;
    private ArrayList<Folder> mDirPaths = new ArrayList<>();
    /**
     * 已选择的图片
     */
    private ArrayList<String> selectedPicture = new ArrayList<>();
    private String cameraPath = null;
    private Folder imageAll, currentImageFolder;
    private FolderAdapter dirAdapter;
    private HImageLoader imageLoader;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_grid_picture);
        bindViews();
        initViews();
        bindListeners();
    }

    public void back(View view) {
        onBackPressed();
    }

    private void bindViews() {
        view_layer = findViewById(R.id.view_layer);
        tv_title = (TextView) findViewById(R.id.tv_title);
        recycler_image = (RecyclerView) findViewById(R.id.recycler_view);
        recycler_dir = (RecyclerView) findViewById(R.id.recycler_dir);
        view_layer.setVisibility(View.GONE);
    }

    private void initViews() {
        imageLoader = HGallery.getImageLoader(this);
        imageAll = new Folder();
        imageAll.setDir("/所有图片");
        currentImageFolder = imageAll;
        mDirPaths.add(imageAll);
        getThumbnail();
    }

    private void bindListeners() {
        tv_title.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toggleDirlistPop();
            }
        });

        //图片列表
        GridLayoutManager mgr = new GridLayoutManager(this, 3);
        recycler_image.setLayoutManager(mgr);
        recycler_image.addItemDecoration(new GridSpacingItemDecoration(3, dp2px(5), false));
        imageAdapter = new ImageAdapter();
        recycler_image.setAdapter(imageAdapter);

        //目录列表
        recycler_dir.setLayoutManager(new LinearLayoutManager(this));
        dirAdapter = new FolderAdapter();
        recycler_dir.setAdapter(dirAdapter);

        //黑色图层
        view_layer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hideDirList();
            }
        });
    }

    private void toggleDirlistPop() {
        if (isDirShowing) {
            hideDirList();
        } else {
            showDirlist();
        }
    }

    private void hideDirList() {
        view_layer.animate().alpha(0).setDuration(300).start();

        recycler_dir.animate().translationY(-dp2px(310)).setDuration(300).setListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                isDirShowing = false;
                view_layer.setVisibility(View.GONE);
            }
        }).start();
    }

    private void showDirlist() {
        view_layer.setVisibility(View.VISIBLE);
        view_layer.animate().alpha(1).setDuration(300).start();
        recycler_dir.animate().translationY(0).setDuration(300).setListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                isDirShowing = true;
            }
        }).start();
    }


    /**
     * 得到缩略图
     */
    private void getThumbnail() {
        /**
         * 临时的辅助类，用于防止同一个文件夹的多次扫描
         */
        HashMap<String, Integer> tmpDir = new HashMap<String, Integer>();

        Cursor mCursor = getContentResolver().query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                new String[]{MediaStore.Images.ImageColumns.DATA}, "", null,
                MediaStore.MediaColumns.DATE_ADDED + " DESC");
        Log.e("TAG", mCursor.getCount() + "");
        if (mCursor.moveToFirst()) {
            do {
                // 获取图片的路径
                String path = mCursor.getString(0);
                Log.e("TAG", path);
                File imgFile = new File(path);
                if (imgFile.length() > 0)
                    imageAll.images.add(new ImageItem(path));
                // 获取该图片的父路径名
                if (imgFile.getParentFile() == null) {
                    continue;
                }
                Folder imageFloder = null;
                String dirPath = imgFile.getParentFile().getAbsolutePath();
                if (!tmpDir.containsKey(dirPath)) {
                    // 初始化imageFloder
                    imageFloder = new Folder();
                    imageFloder.setDir(dirPath);
                    imageFloder.setFirstImagePath(path);
                    mDirPaths.add(imageFloder);
                    Log.d("GridImageActivity", dirPath + "," + path);
                    tmpDir.put(dirPath, mDirPaths.indexOf(imageFloder));
                } else {
                    imageFloder = mDirPaths.get(tmpDir.get(dirPath));
                }
                if (imgFile.length() > 0)
                    imageFloder.images.add(new ImageItem(path));
            } while (mCursor.moveToNext());
        }
        mCursor.close();
        for (int i = 0; i < mDirPaths.size(); i++) {
            Folder f = mDirPaths.get(i);
            Log.d("GridImageActivity", i + "-----" + f.getName() + "---" + f.images.size());
        }
        tmpDir = null;
    }

    private void resetDirList(Folder selectFolder) {
        Folder bufer = new Folder();
        bufer.images.addAll(selectFolder.images);
        bufer.images.add(0, new ImageItem(""));
        bufer.dir = selectFolder.dir;
        bufer.firstImagePath = selectFolder.firstImagePath;
        bufer.name = selectFolder.name;
        currentImageFolder = bufer;
        dirAdapter.notifyDataSetChanged();
        imageAdapter.notifyDataSetChanged();
        hideDirList();
    }

    public int dp2px(int dp) {
        float scale = getResources().getDisplayMetrics().densityDpi;
        return (int) (dp * scale / 160 + 0.5f);
    }

    private void showToast(String string) {
        Toast.makeText(getApplicationContext(), string, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case UCrop.REQUEST_CROP:
                    onResultCrop();
                    break;
                case UCrop.RESULT_ERROR:
                    break;
                case RESULT_CROP:
                    this.setResult(RESULT_OK, data);
                    finish();
                    break;
                default:
                    break;
            }
        }

    }


    //照相机返回图片后裁切处理
    private void onResultCrop() {
        Uri destinationUri = Uri.fromFile(new File(getCacheDir(), mTackPhotoName));
        UCrop.Options options = new UCrop.Options();
        options.setStatusBarColor(Color.BLACK);
        options.setToolbarColor(Color.WHITE);
        options.setToolbarWidgetColor(Color.BLACK);
        options.setActiveWidgetColor(Color.RED);
        UCrop.of(mTakePhotoUri, destinationUri)
                .withOptions(options)
                .start(GridImageActivity.this, this.RESULT_CROP);
    }

    class ImageAdapter extends RecyclerView.Adapter<ImageViewHolder> {

        @Override
        public ImageViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            if (viewType == 0) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_photo_list_camera, parent, false);
                return new ImageViewHolder(view);
            }
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_image, parent, false);
            return new ImageViewHolder(view);
        }

        @Override
        public void onBindViewHolder(ImageViewHolder holder, int position) {
            if (position == 0) {
                holder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        takePhotoAction();
                    }
                });

                return;
            } else {
                final ImageItem item = currentImageFolder.images.get(position);
                imageLoader.displayImage(holder.imageView, item.path);
                holder.tv_click.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        String destinationFileName = item.path.hashCode() + "";
                        Uri sourceUri = Uri.fromFile(new File(item.path));
                        Uri destinationUri = Uri.fromFile(new File(getCacheDir(), destinationFileName));
                        UCrop.Options options = new UCrop.Options();
                        options.setStatusBarColor(Color.BLACK);
                        options.setToolbarColor(Color.WHITE);
                        options.setToolbarWidgetColor(Color.BLACK);
                        options.setActiveWidgetColor(Color.RED);
                        UCrop.of(sourceUri, destinationUri)
                                .withOptions(options)
                                .start(GridImageActivity.this, GridImageActivity.RESULT_CROP);
                        // ClipImageActivity.launch(GridImageActivity.this, item.path);
                    }
                });
            }

        }

        @Override
        public int getItemViewType(int position) {
            return position;
        }

        @Override
        public int getItemCount() {
            return currentImageFolder.images.size();
        }
    }

    class ImageViewHolder extends RecyclerView.ViewHolder {

        public ImageView imageView;
        public View tv_click;

        public ImageViewHolder(View itemView) {
            super(itemView);
            imageView = (ImageView) itemView.findViewById(R.id.iv);
            tv_click = itemView.findViewById(R.id.tv_click);
        }

    }

    class FolderAdapter extends RecyclerView.Adapter<FolderViewHolder> {
        @Override
        public FolderViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_dir, parent, false);
            return new FolderViewHolder(view);
        }

        @Override
        public void onBindViewHolder(FolderViewHolder holder, int position) {
            final Folder item = mDirPaths.get(position);
            imageLoader.displayImage(holder.iv_dir, item.getFirstImagePath());
            String name = item.name.startsWith("/") ? item.name.substring(1) : item.name;
            holder.tv_dirname.setText(name + " (" + (item.images.size()) + "张) ");
            holder.ll_root.setSelected(currentImageFolder == item);
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    resetDirList(item);
                }
            });
        }

        @Override
        public int getItemCount() {
            return mDirPaths.size();
        }
    }

    class FolderViewHolder extends RecyclerView.ViewHolder {

        public ImageView iv_dir;
        public TextView tv_dirname;
        public View ll_root;

        public FolderViewHolder(View itemView) {
            super(itemView);
            ll_root = itemView.findViewById(R.id.ll_root);
            iv_dir = (ImageView) itemView.findViewById(R.id.iv_dir);
            tv_dirname = (TextView) itemView.findViewById(R.id.tv_dirname);
        }
    }

    /**
     * 拍照
     */
    protected void takePhotoAction() {
        if (!FileUtils.existSDCard()) {
            Toast.makeText(GridImageActivity.this, "没有检测到SD卡", Toast.LENGTH_SHORT).show();
            return;
        }

        File takePhotoFolder = null;
        if (TextUtils.isEmpty(mPhotoTargetFolder)) {
            takePhotoFolder = FileUtils.getTakePhotoDir();
        } else {
            takePhotoFolder = new File(mPhotoTargetFolder);
        }

        SimpleDateFormat f = new SimpleDateFormat("yyyyMMddHHmmss", Locale.getDefault());
        mTackPhotoName = f.format(new Date());
        File toFile = new File(takePhotoFolder, "IMG" + mTackPhotoName + ".jpg");
        boolean suc = false;
        try {
            suc = toFile.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
//        Logger.d("create folder=" + toFile.getAbsolutePath());
        if (suc) {
            mTakePhotoUri = Uri.fromFile(toFile);
            Intent captureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            captureIntent.putExtra(MediaStore.EXTRA_OUTPUT, mTakePhotoUri);
            startActivityForResult(captureIntent, UCrop.REQUEST_CROP);
        } else {
//            Logger.e("create file failure");
            Toast.makeText(GridImageActivity.this, ",0", Toast.LENGTH_SHORT).show();
        }
    }

    class Folder {


        public List<ImageItem> images = new ArrayList<ImageItem>();
        /**
         * 图片的文件夹路径
         */
        private String dir;
        /**
         * 第一张图片的路径
         */
        private String firstImagePath;
        /**
         * 文件夹的名称
         */
        private String name;

        public String getDir() {
            return dir;
        }

        public void setDir(String dir) {
            this.dir = dir;
            int lastIndexOf = this.dir.lastIndexOf("/");
            this.name = this.dir.substring(lastIndexOf);
        }

        public String getFirstImagePath() {
            return firstImagePath;
        }

        public void setFirstImagePath(String firstImagePath) {
            this.firstImagePath = firstImagePath;
        }

        public String getName() {
            return name;
        }

    }

    class ImageItem {

        String path;
        Boolean isCheck;

        public Boolean getCheck() {
            return isCheck;
        }

        public void setCheck(Boolean check) {
            isCheck = check;
        }

        public ImageItem(String p) {
            this.path = p;
        }

    }

    class GridSpacingItemDecoration extends RecyclerView.ItemDecoration {
        private int spanCount;
        private int spacing;
        private boolean includeEdge;

        public GridSpacingItemDecoration(int spanCount, int spacing, boolean includeEdge) {
            this.spanCount = spanCount;
            this.spacing = spacing;
            this.includeEdge = includeEdge;
        }

        @Override
        public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
            int position = parent.getChildAdapterPosition(view); // item position
            int column = position % spanCount; // item column

            if (includeEdge) {
                outRect.left = spacing - column * spacing / spanCount; // spacing - column * ((1f / spanCount) * spacing)
                outRect.right = (column + 1) * spacing / spanCount; // (column + 1) * ((1f / spanCount) * spacing)

                if (position < spanCount) { // top edge
                    outRect.top = spacing;
                }
                outRect.bottom = spacing; // item bottom
            } else {
                outRect.left = column * spacing / spanCount; // column * ((1f / spanCount) * spacing)
                outRect.right = spacing - (column + 1) * spacing / spanCount; // spacing - (column + 1) * ((1f /    spanCount) * spacing)
                if (position >= spanCount) {
                    outRect.top = spacing; // item top
                }
            }
        }
    }
}
