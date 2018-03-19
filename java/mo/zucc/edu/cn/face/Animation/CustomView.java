package mo.zucc.edu.cn.face.Animation;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PaintFlagsDrawFilter;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

import mo.zucc.edu.cn.face.R;

/**
 * Created by Mo on 2017/12/18.
 */

public class CustomView extends View implements Runnable {
    private Paint paint;// 画笔
    private Context mContext;// 上下文环境引用
    private int radiu = 150;
    private int x = 50;
    private int flag = 0;
    private int startx;
    private int starty;
    float times = 1;
    private Rect face = new Rect();
    private Rect dst = new Rect();
    private Rect src = new Rect();
    private int lag = 0;
    float scale = 1.0f;
    public CustomView(Context context) {
        this(context, null);
    }

    public CustomView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;

        // 初始化画笔
        initPaint();
    }

    public void  Customgetdata(Rect rect,Rect src ,Rect dst,float scale,float time){
        this.startx = rect.left;
        this.starty = rect.top;
        this.radiu = (rect.width());
        lag = 1;
        this.times = time / 250;
//        this.startx = (int)(DensityUtil.dip2px(getContext(),rect.left)*((float) dst.width() / (float) src.width()))+(int)(dst.left / scale);
//        this.starty =(int)(DensityUtil.dip2px(getContext(),rect.left)*((float) dst.height() / (float) src.height()))+(int)(dst.top / scale);
//        this.radiu = (DensityUtil.dip2px(getContext(),rect.bottom)-starty)/2;
//        this.radiu = rect.width()/2;
//        this.startx = (int)(rect.left*((float) dst.width() / (float) src.width()));
//        this.startx = (int)(rect.left+((float) dst.width() / (float) src.width())*(int)(dst.left / scale));
//        this.starty =(int)(rect.top*((float) dst.height() / (float) src.height()))+100;
        this.dst = dst;
        this.src = src;
//        rect.left = (int)(dst.left/scale)+rect.left;
//        rect.top = (int)(dst.top/scale)+rect.top;
        this.face = rect;
        this.scale = scale;
    }
    /**
     * 初始化画笔
     */
    private void initPaint() {
        // 实例化画笔并打开抗锯齿
        paint = new Paint(); //设置一个笔刷大小是3的黄色的画笔
        paint.setColor(Color.BLUE);                    //设置画笔颜色
        paint.setStrokeWidth((float) 5.0*times);              //线宽
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeJoin(Paint.Join.ROUND);
        paint.setStrokeCap(Paint.Cap.ROUND);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if(lag != 0){
            canvas.scale((float) dst.width() / (float) src.width(), (float) dst.height() / (float) src.height());   //缩放
            canvas.translate(dst.left / scale, dst.top / scale);    //移动canvas

            canvas.setDrawFilter(new PaintFlagsDrawFilter(0, Paint.ANTI_ALIAS_FLAG|Paint.FILTER_BITMAP_FLAG));
            paint.setColor(this.getResources().getColor(R.color.wai));
            paint.setStrokeWidth((float) 16.0*times);
            canvas.drawCircle(startx+radiu/2,starty+radiu/2,radiu/2,paint);


            RectF rect = new RectF(startx, starty, startx+radiu, starty+radiu);
            if(flag == 0 ){
                paint.setColor(this.getResources().getColor(R.color.lanquan));
            }else {
                paint.setColor(this.getResources().getColor(R.color.nei));
            }
            paint.setStrokeWidth((float) 5.0*times);
            canvas.drawArc(rect,54-x,162,false,paint);
            canvas.drawArc(rect,36-x,-162,false,paint);

            RectF rect1 = new RectF(startx+8*times, starty+8*times, startx+radiu-8*times, starty-8*times+radiu);

            paint.setColor(this.getResources().getColor(R.color.nei));

            paint.setStrokeWidth((float) 1.5*times);
            canvas.drawArc(rect1,180+x,36,false,paint);
            canvas.drawArc(rect1,300+x,36,false,paint);
            canvas.drawArc(rect1,60+x,36,false,paint);

            RectF rect2 = new RectF(startx-8*times, starty-8*times, startx+radiu+8*times, starty+radiu+8*times        );

            paint.setColor(this.getResources().getColor(R.color.wai));
            paint.setStrokeWidth((float) 3.0*times);
            canvas.drawArc(rect2,160+x,120,false,paint);
            canvas.drawArc(rect2,-20+x,120,false,paint);

        }
    }

    @Override
    public void run() {
	/*
	 * 确保线程不断执行不断刷新界面
	 */ double y = 0;
        int time = 0 ;
        while (true) {

            try {
			/*
			 * 如果半径小于200则自加否则大于200后重置半径值以实现往复
			 */
                double z = 200*Math.sin(y*Math.PI/180);
                x = (int)z;
                y = y + 5;
                postInvalidate();
                if(y>200){
                    flag=1;
                    x=0;
                    time++;
                    if(time <= 20){
                        radiu -= 2;
                        startx += 1;
                        starty += 1;
                    }
                }
                // 每执行一次暂停40毫秒
                Thread.sleep(40);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}