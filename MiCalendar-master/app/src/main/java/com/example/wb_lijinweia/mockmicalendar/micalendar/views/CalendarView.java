/*
 * Copyright (c) 2016.
 * wb-lijinwei.a@alibaba-inc.com
 */

package com.example.wb_lijinweia.mockmicalendar.micalendar.views;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;

import com.example.wb_lijinweia.mockmicalendar.micalendar.model.CustomDate;
import com.example.wb_lijinweia.mockmicalendar.micalendar.utils.DateUtil;
import com.example.wb_lijinweia.mockmicalendar.micalendar.utils.LunarCalendar;

public class CalendarView extends View {
	private static final String TAG = "CalendarView";
	/**
	 * 两种模式 （月份和星期）
	 */
	public static final int MONTH_STYLE = 0;
	public static final int WEEK_STYLE = 1;

	private static final int TOTAL_COL = 7;
	private static final int TOTAL_ROW = 6;

	private Paint mCirclePaint;	// 绘制圆形的画笔
	private Paint mMarkCirclePaint;
	private Paint mTextPaint;	// 绘制文本的画笔
	private Paint mLunarTextPaint;
	private int mViewWidth;	// 视图的宽度
	private int mViewHight;	// 视图的高度
	private int mCellSpace;	// 单元格间距
	private Row rows[] = new Row[TOTAL_ROW];	// 行数组，每个元素代表一行

	private CustomDate mShowDate;//自定义的日期  包括year month day
	public static int style = MONTH_STYLE;
	private static final int WEEK = 7;
	private OnCellCallBack mOnCellCallBack;	// 单元格点击回调事件
	private int touchSlop;
	private boolean callBackCellSpace;

	private LunarCalendar lunarCalendar = null;

	public interface OnCellCallBack {

		void clickDate(CustomDate date);//回调点击的日期

		void clickDatePosition(CustomDate date, State state);//回调点击的日期

		void clickPosition(int position);

		void onMesureCellHeight(int cellSpace);//回调cell的高度确定slidingDrawer高度

		void changeDate(CustomDate date);//回调滑动viewPager改变的日期

		void init(CustomDate date);
	}

	public CalendarView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init(context);
	}

	public CalendarView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context);
	}

	public CalendarView(Context context) {
		super(context);
		init(context);
	}

	public CalendarView(Context context, int style, OnCellCallBack mOnCellCallBack) {
		super(context);
		CalendarView.style = style;
		this.mOnCellCallBack = mOnCellCallBack;
		init(context);
	}

	@Override
	protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
		super.onLayout(changed, left, top, right, bottom);
	}

	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		for (int i = 0; i < TOTAL_ROW; i++) {
			if (rows[i] != null)
				rows[i].drawCells(canvas);
		}
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		if(mCellSpace > 0){
			int h = MeasureSpec.makeMeasureSpec(mCellSpace * TOTAL_ROW,
					MeasureSpec.EXACTLY);
			super.onMeasure(widthMeasureSpec, h);
		}else {
			super.onMeasure(widthMeasureSpec, heightMeasureSpec);
		}
	}

	private void init(Context context) {
		lunarCalendar = new LunarCalendar();
		mTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		mLunarTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		mCirclePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		mCirclePaint.setStyle(Paint.Style.FILL);
		mCirclePaint.setColor(Color.parseColor("#F24949"));	// 红色圆形
		mMarkCirclePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		mMarkCirclePaint.setStyle(Paint.Style.FILL);
		mMarkCirclePaint.setColor(Color.parseColor("#F24949"));
		touchSlop = ViewConfiguration.get(context).getScaledTouchSlop();
		initDate();
	}

	private void initDate() {
		if (style == MONTH_STYLE) {
			if(mShowDate == null){
				mShowDate = new CustomDate();
			}

		} else if(style == WEEK_STYLE ) {
			if(mShowDate == null){
				mShowDate = DateUtil.getNextSunday();
			}
		}
		fillDate();
	}

	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		super.onSizeChanged(w, h, oldw, oldh);
		mViewWidth = w;
		mViewHight = h;
		mCellSpace = Math.min(mViewHight / TOTAL_ROW, mViewWidth / TOTAL_COL);
		if (!callBackCellSpace) {
			if(mOnCellCallBack != null){
				mOnCellCallBack.onMesureCellHeight(mCellSpace);
			}

			callBackCellSpace = true;
		}
		mTextPaint.setTextSize(mCellSpace / 3);
		mLunarTextPaint.setTextSize(mCellSpace / 6);
	}

	private Cell mTodayCell;
	private Cell mClickedCell;
	private float mDownX;
	private float mDownY;
	/*
     *
     * 触摸事件为了确定点击的位置日期
     */
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		switch (event.getAction()) {
			case MotionEvent.ACTION_DOWN:
				mDownX = event.getX();
				mDownY = event.getY();
				break;
			case MotionEvent.ACTION_UP:
				float disX = event.getX() - mDownX;
				float disY = event.getY() - mDownY;
				if (Math.abs(disX) < touchSlop && Math.abs(disY) < touchSlop) {
					int col = (int) (mDownX / mCellSpace);
					int row = (int) (mDownY / mCellSpace);
					measureClickCell(col, row);
				}
				break;
		}
		return true;
	}

	private void measureClickCell(int col, int row) {
		if (col >= TOTAL_COL || row >= TOTAL_ROW)
			return;
		/*if (mTodayCell != null) {
			rows[mTodayCell.j].cells[mTodayCell.i] = mTodayCell;
		}*/
		if (mClickedCell != null) {	//clear previous clicked
			rows[mClickedCell.j].cells[mClickedCell.i].state = State.CURRENT_MONTH_DAY;
		}

		if (rows[row] != null) {
			/*mTodayCell = new Cell(rows[row].cells[col].date,
					rows[row].cells[col].state, rows[row].cells[col].i,
					rows[row].cells[col].j);*/

			if(mOnCellCallBack != null){
				if(rows[row].cells[col].state == State.NEXT_MONTH_DAY ||
						rows[row].cells[col].state == State.PAST_MONTH_DAY){
					mShowDate = rows[row].cells[col].date;
					mOnCellCallBack.clickDatePosition(rows[row].cells[col].date, rows[row].cells[col].state);
					return;
				}
			}

			rows[row].cells[col].state = State.CLICK_DAY;
			CustomDate date = rows[row].cells[col].date;
			date.week = col;
			mClickedCell = rows[row].cells[col];
			if(mOnCellCallBack != null){
				mOnCellCallBack.clickDate(date);
				mOnCellCallBack.clickPosition(row * TOTAL_COL + col);
			}
			invalidate();
		}
	}

	// 组
	class Row {
		public int j;
		Row(int j) {
			this.j = j;
		}

		public Cell[] cells = new Cell[TOTAL_COL];

		public void drawCells(Canvas canvas) {
			for (int i = 0; i < cells.length; i++) {
				if (cells[i] != null)
					cells[i].drawSelf(canvas);
			}

		}
	}

	// 单元格
	class Cell {
		public CustomDate date;
		public State state;
		public int i;
		public int j;

		public Cell(CustomDate date, State state, int i, int j) {
			super();
			this.date = date;
			this.state = state;
			this.i = i;
			this.j = j;
		}


		// 绘制一个单元格 如果颜色需要自定义可以修改
		public void drawSelf(Canvas canvas) {
			switch (state) {
				case CURRENT_MONTH_DAY:
					mTextPaint.setColor(Color.parseColor("#f0000000"));
					break;
				case NEXT_MONTH_DAY:
				case PAST_MONTH_DAY:
					mTextPaint.setColor(Color.parseColor("#40000000"));
					break;
				case TODAY:
					mTextPaint.setColor(Color.parseColor("#F24949"));
					break;
				case CLICK_DAY:
					mTextPaint.setColor(Color.parseColor("#fffffe"));
					canvas.drawCircle((float) (mCellSpace * (i + 0.5)),
							(float) ((j + 0.5) * mCellSpace), mCellSpace / 2,
							mCirclePaint);
					break;
			}
			mLunarTextPaint.setColor(Color.parseColor("#40000000"));
			// 绘制文字
			String content = date.day+"";
			String lunarContent = lunarCalendar.getLunarDate(date.getYear(), date.getMonth(), date.getDay(), true);
			if(DateUtil.isToday(date)){
				content = "今日";
			}
			canvas.drawText(content,
					(float) ((i + 0.5) * mCellSpace - mTextPaint.measureText(content)/2),
					(float) ((j + 0.7) * mCellSpace - mTextPaint.measureText(content, 0, 1) / 2 - 10),
					mTextPaint);

			canvas.drawText(lunarContent,
					(float) ((i + 0.5) * mCellSpace - mLunarTextPaint.measureText(lunarContent)/2),
					(float) ((j + 0.7) * mCellSpace - mLunarTextPaint.measureText(lunarContent, 0, 1) / 2 + 15),
					mLunarTextPaint);

			//draw mark
			if((j * TOTAL_COL + i) % 10 == 0){
				canvas.drawCircle((float) (mCellSpace * (i + 0.5)),
						(float) (j * mCellSpace) + 6, 4,
						mMarkCirclePaint);
			}


		}
	}
	/**
	 *
	 * @author huang
	 * cell的state
	 *当前月日期，过去的月的日期，下个月的日期，今天，点击的日期
	 *
	 */
	public enum State {
		CURRENT_MONTH_DAY, PAST_MONTH_DAY, NEXT_MONTH_DAY, TODAY, CLICK_DAY;
	}

	/**
	 * 填充日期的数据
	 */
	private void fillDate() {
		if (style == MONTH_STYLE) {
			fillMonthDate();
		} else if(style == WEEK_STYLE) {
			fillWeekDate();
		}
		if(mOnCellCallBack != null){
			mOnCellCallBack.changeDate(mShowDate);
		}

	}

	/**
	 * 填充星期模式下的数据 默认通过当前日期得到所在星期天的日期，然后依次填充日期
	 */
	private void fillWeekDate() {
		int lastMonthDays = DateUtil.getMonthDays(mShowDate.year, mShowDate.month-1);
		rows[0] = new Row(0);
		int day = mShowDate.day;
		for (int i = TOTAL_COL -1; i >= 0 ; i--) {
			day -= 1;
			if (day < 1) {
				day = lastMonthDays;
			}
			CustomDate date = CustomDate.modifiDayForObject(mShowDate, day);
			if (DateUtil.isToday(date)) {
				mTodayCell = new Cell(date, State.TODAY, i, 0);
				date.week = i;
				if(mOnCellCallBack != null){
					mOnCellCallBack.clickDate(date);
				}

				rows[0].cells[i] =  new Cell(date, State.CLICK_DAY, i, 0);
				continue;
			}
			rows[0].cells[i] = new Cell(date, State.CURRENT_MONTH_DAY,i, 0);
		}
	}

	/**
	 * 填充月份模式下数据 通过getWeekDayFromDate得到一个月第一天是星期几就可以算出所有的日期的位置 然后依次填充
	 * 这里最好重构一下
	 */
	private void fillMonthDate() {
		int monthDay = DateUtil.getCurrentMonthDay();
		int lastMonthDays = DateUtil.getMonthDays(mShowDate.year, mShowDate.month - 1);	// 上个月的天数
		int currentMonthDays = DateUtil.getMonthDays(mShowDate.year, mShowDate.month);	// 当前月的天数
		int firstDayWeek = DateUtil.getWeekDayFromDate(mShowDate.year, mShowDate.month);
		boolean isCurrentMonth = false;
		if (DateUtil.isCurrentMonth(mShowDate)) {
			isCurrentMonth = true;
		}
		int day = 0;
		for (int j = 0; j < TOTAL_ROW; j++) {
			rows[j] = new Row(j);
			for (int i = 0; i < TOTAL_COL; i++) {
				int postion = i + j * TOTAL_COL;	// 单元格位置
				if (postion >= firstDayWeek
						&& postion < firstDayWeek + currentMonthDays) {	// 这个月的
					day++;
					if (isCurrentMonth && day == monthDay) {
						CustomDate date = CustomDate.modifiDayForObject(mShowDate, day);
						mTodayCell = new Cell(date,State.TODAY, i,j);
						date.week = i;
						rows[j].cells[i] = mTodayCell;
						continue;
					}
					rows[j].cells[i] = new Cell(CustomDate.modifiDayForObject(mShowDate, day),
							State.CURRENT_MONTH_DAY, i, j);
				} else if (postion < firstDayWeek) { //last month
					rows[j].cells[i] = new Cell(new CustomDate(mShowDate.year, mShowDate.month-1, lastMonthDays - (firstDayWeek- postion - 1)), State.PAST_MONTH_DAY, i, j);
				} else if (postion >= firstDayWeek + currentMonthDays) {	//next month
					rows[j].cells[i] = new Cell((new CustomDate(mShowDate.year, mShowDate.month+1, postion - firstDayWeek - currentMonthDays + 1)), State.NEXT_MONTH_DAY, i, j);
				}
			}
		}
	}

	public void update() {
		fillDate();
		invalidate();
	}

	public void backToday(){
		mShowDate = new CustomDate();
		initDate();
		invalidate();
	}
	//切换style
	public void switchStyle(int style) {
		CalendarView.style = style;
		if (style == MONTH_STYLE) {
			update();
		} else if (style == WEEK_STYLE) {
			int firstDayWeek = DateUtil.getWeekDayFromDate(mShowDate.year,
					mShowDate.month);
			int day =  1 + WEEK - firstDayWeek;
			mShowDate.day = day;

			update();
		}

	}
	//向右滑动
	public void rightSilde() {
		if (style == MONTH_STYLE) {
			if (mShowDate.month == 12) {
				mShowDate.month = 1;
				mShowDate.year += 1;
			} else {
				mShowDate.month += 1;
			}
		} else if (style == WEEK_STYLE) {
			int currentMonthDays = DateUtil.getMonthDays(mShowDate.year, mShowDate.month);
			if (mShowDate.day + WEEK > currentMonthDays) {
				if (mShowDate.month == 12) {
					mShowDate.month = 1;
					mShowDate.year += 1;
				} else {
					mShowDate.month += 1;
				}
				mShowDate.day = WEEK - currentMonthDays + mShowDate.day;
			}else{
				mShowDate.day += WEEK;
			}
		}
		update();
	}
	//向左滑动
	public void leftSilde() {
		if (style == MONTH_STYLE) {
			if (mShowDate.month == 1) {
				mShowDate.month = 12;
				mShowDate.year -= 1;
			} else {
				mShowDate.month -= 1;
			}

		} else if (style == WEEK_STYLE) {
			int lastMonthDays = DateUtil.getMonthDays(mShowDate.year, mShowDate.month);
			if (mShowDate.day - WEEK < 1) {
				if (mShowDate.month == 1) {
					mShowDate.month = 12;
					mShowDate.year -= 1;
				} else {
					mShowDate.month -= 1;
				}
				mShowDate.day = lastMonthDays - WEEK + mShowDate.day;

			}else{
				mShowDate.day -= WEEK;
			}
			Log.i(TAG, "leftSilde"+mShowDate.toString());
		}
		update();
	}

	public void setmShowDate(CustomDate mShowDate) {
		if(mShowDate != null){
			this.mShowDate = mShowDate;
		}else {
			this.mShowDate = new CustomDate();
		}

		update();
	}

	public CustomDate getmShowDate() {
		return this.mShowDate;
	}

	public void setSelect(CustomDate date){
		if(date == null){
			return;
		}
		for(int i = 0; i < TOTAL_ROW; i++){
			for(int j = 0; j < TOTAL_COL; j++){
				if(date.equals(rows[i].cells[j].date)){
					measureClickCell(j, i);
				}
			}
		}

	}

	public void setInitPage(){
		if(mOnCellCallBack != null){
			mOnCellCallBack.init(mShowDate);
		}
	}
}
