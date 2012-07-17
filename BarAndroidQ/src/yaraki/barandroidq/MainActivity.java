
package yaraki.barandroidq;

import android.app.Activity;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

public class MainActivity extends Activity {

    /** 一回のイベントで何度回転するか */
    private static final float TICK_DEGREE = 10.f;

    /** 画像を表示する大きさ。幅 (= 高さ) の半分 */
    private static final int HALF_SIZE = 200;

    /** 描画頻度 (ミリ秒) */
    private static final long FRAME_MILLISECS = 1000 / 60;

    private FieldUpdater mUpdater;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    @Override
    protected void onResume() {
        super.onResume();
        SurfaceView field = (SurfaceView)findViewById(R.id.field);
        mUpdater = new FieldUpdater(this);
        mUpdater.start(field.getHolder());
    }

    @Override
    protected void onPause() {
        mUpdater.stop();
        super.onPause();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_main, menu);
        return true;
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_VOLUME_UP: {
                mUpdater.rotate(Direction.RIGHT);
                return true;
            }
            case KeyEvent.KEYCODE_VOLUME_DOWN: {
                mUpdater.rotate(Direction.LEFT);
                return true;
            }
            case KeyEvent.KEYCODE_VOLUME_MUTE: {
                // 割り当てていないが、ミュート切り替えの表示を阻止すべくイベントを取り上げる
                return true;
            }
        }
        Log.d("tmp", "onKeyDown: " + keyCode);
        return super.onKeyDown(keyCode, event);
    }

    private static class FieldUpdater implements SurfaceHolder.Callback, Runnable {

        private SurfaceHolder mHolder;

        private Drawable mImage;

        private int mCenterX;

        private int mCenterY;

        /** 描画スレッドが停止しつつある、または停止したなら true */
        private boolean mStopped = false;

        /** 現在の角度 */
        private float mAngle = 0.f;

        public FieldUpdater(Context context) {
            mImage = context.getResources().getDrawable(R.drawable.android);
        }

        /** SurfaceView に対して描画を開始する */
        public void start(SurfaceHolder holder) {
            holder.addCallback(this);
            new Thread(this).start();
        }

        public void stop() {
            mStopped = true;
        }

        public void rotate(int direction) {
            switch (direction) {
                case Direction.RIGHT: {
                    mAngle += TICK_DEGREE;
                    break;
                }
                case Direction.LEFT: {
                    mAngle -= TICK_DEGREE;
                    break;
                }
            }
        }

        @Override
        public void run() {
            try {
                while (true) {
                    if (mStopped) {
                        break;
                    }
                    if (null != mHolder) {
                        Canvas canvas = mHolder.lockCanvas();
                        if (null != canvas) {
                            redraw(canvas);
                            mHolder.unlockCanvasAndPost(canvas);
                        }
                    }
                    Thread.sleep(FRAME_MILLISECS);
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

        @Override
        public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
            mCenterX = width / 2;
            mCenterY = height / 2;
            mImage.setBounds(mCenterX - HALF_SIZE, mCenterY - HALF_SIZE, //
                    mCenterX + HALF_SIZE, mCenterY + HALF_SIZE);
        }

        @Override
        public void surfaceCreated(SurfaceHolder holder) {
            mHolder = holder;
        }

        @Override
        public void surfaceDestroyed(SurfaceHolder holder) {
            stop();
        }

        private void redraw(Canvas canvas) {
            // 背景
            canvas.drawColor(Color.WHITE);
            // 画像
            if (null != mImage) {
                canvas.rotate(mAngle, mCenterX, mCenterY);
                mImage.draw(canvas);
            }
        }
    }

    public interface Direction {

        public static final int RIGHT = 1;

        public static final int LEFT = 2;
    }
}
