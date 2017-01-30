package com.example.wb_lijinweia.mockmicalendar;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.example.wb_lijinweia.mockmicalendar.micalendar.views.CalendarView;
import com.example.wb_lijinweia.mockmicalendar.micalendar.adpter.CalendarViewAdapter;
import com.example.wb_lijinweia.mockmicalendar.micalendar.model.CustomDate;
import com.example.wb_lijinweia.mockmicalendar.micalendar.MonthPager;
import com.example.wb_lijinweia.mockmicalendar.micalendar.adpter.NormalRecyclerViewAdapter;
import com.example.wb_lijinweia.mockmicalendar.micalendar.views.CircleTextView;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private MonthPager mViewPager;
    private CalendarView[] mShowViews;
    private CalendarViewAdapter<CalendarView> adapter;

    private RecyclerView rvToDoList;
    private TextView textViewYearDisplay;
    private TextView textViewMonthDisplay;
    private TextView textViewWeekDisplay;
    private TextView monthCalendarView;
    private TextView weekCalendarView;
    private CircleTextView today;
    private CalendarView[] viewsMonth;
    private CalendarView.OnCellCallBack mCallback;
    private int mCurrentPage = MonthPager.CURRENT_DAY_INDEX;
    private CustomDate lastClickCustomDate;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        textViewYearDisplay = (TextView) findViewById(R.id.show_year_view);
        textViewMonthDisplay = (TextView) findViewById(R.id.show_month_view);
        textViewWeekDisplay = (TextView) findViewById(R.id.show_week_view);
        monthCalendarView = (TextView) this.findViewById(R.id.month_calendar_button);
        weekCalendarView = (TextView) this.findViewById(R.id.week_calendar_button);
        today = (CircleTextView) findViewById(R.id.now_circle_view);

        mViewPager = (MonthPager) this.findViewById(R.id.vp_calendar);

        rvToDoList = (RecyclerView) findViewById(R.id.list);
        rvToDoList.setHasFixedSize(true);
        rvToDoList.setLayoutManager(new LinearLayoutManager(this));//这里用线性显示 类似于listview
        rvToDoList.setAdapter(new NormalRecyclerViewAdapter(this));
        
        mCallback = new CalendarView.OnCellCallBack() {
            @Override
            public void clickDate(CustomDate date) {
                textViewYearDisplay.setText(date.getYear() + "");
                textViewMonthDisplay.setText(date.getMonth() + "月");
                textViewWeekDisplay.setText(date.getDisplayWeek(date.getWeek()) + "");
            }

            @Override
            public void clickDatePosition(CustomDate date, CalendarView.State state) {
                lastClickCustomDate = date;
                switch (state){
                    case CURRENT_MONTH_DAY:
                        break;
                    case PAST_MONTH_DAY:
                        mViewPager.setCurrentItem(mCurrentPage - 1);
                        break;
                    case NEXT_MONTH_DAY:
                        mViewPager.setCurrentItem(mCurrentPage + 1);
                        break;
                }
            }

            @Override
            public void clickPosition(int position) {
                mViewPager.setSelectedIndex(position);
            }

            @Override
            public void onMesureCellHeight(int cellSpace) {

            }

            @Override
            public void changeDate(CustomDate date) {
                //move to onPageSelected
            }

            @Override
            public void init(CustomDate date) {
                textViewYearDisplay.setText(date.getYear() + "");
                textViewMonthDisplay.setText(date.getMonth() + "月");
                textViewWeekDisplay.setText(date.getDisplayWeek(date.getWeek()) + "");
            }
        };

        viewsMonth = new CalendarView[3];
        for (int i = 0; i < 3; i++) {
            viewsMonth[i] = new CalendarView(this,
                    CalendarView.MONTH_STYLE,
                    mCallback);
        }

        adapter = new CalendarViewAdapter<CalendarView>(viewsMonth);
        setViewPager();

        weekCalendarView.setOnClickListener(this);
        monthCalendarView.setOnClickListener(this);
        today.setOnClickListener(this);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
    }

    private void setViewPager() {
        mViewPager.setAdapter(adapter);
        mViewPager.setCurrentItem(MonthPager.CURRENT_DAY_INDEX);

        mViewPager.setPageTransformer(false, new ViewPager.PageTransformer() {
            @Override
            public void transformPage(View page, float position) {
                position = (float) Math.sqrt(1 - Math.abs(position));
                page.setAlpha(position);
            }
        });
        mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                mCurrentPage = position;
                mShowViews = adapter.getAllItems();

                if(mShowViews[position % mShowViews.length] instanceof CalendarView){
                    CustomDate date = mShowViews[position % mShowViews.length].getmShowDate();
                    textViewYearDisplay.setText(date.getYear() + "");
                    textViewMonthDisplay.setText(date.getMonth() + "月");
                    textViewWeekDisplay.setText(date.getDisplayWeek(date.getWeek()) + "");
                    if(lastClickCustomDate != null){
                        mShowViews[position % mShowViews.length].setSelect(lastClickCustomDate);
                        lastClickCustomDate = null;
                    }else {
                        //mShowViews[position % mShowViews.length].setSelect(date);
                    }
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
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
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.month_calendar_button:
                break;
            case R.id.week_calendar_button:
                break;
            case R.id.now_circle_view:
                CustomDate date = new CustomDate();
                textViewYearDisplay.setText(date.getYear() + "");
                textViewMonthDisplay.setText(date.getMonth() + "月");
                textViewWeekDisplay.setText(date.getDisplayWeek(date.getWeek()) + "");
                adapter.updateDay(date, mCurrentPage);
                break;

        }
    }
}
