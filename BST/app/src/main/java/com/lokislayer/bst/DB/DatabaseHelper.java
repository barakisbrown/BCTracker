package com.lokislayer.bst.DB;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.lokislayer.bst.Model.BloodSugarModel;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by barak on 8/14/2017.
 */

public class DatabaseHelper extends SQLiteOpenHelper
{
    /**
     * The following are constants for use with SQLite Database I am using to store my blood
     * sugar results
     */
    private static final String DB_NAME = "BSResultsDB";
    private static final String TABLE_NAME = "Tests";
    private static final String ID = "id";
    private static final String DATE_TESTED = "date_tested";
    private static final String TIME_TESTED = "time_tested";
    private static final String AMOUNT = "amount";
    private static final int DB_VERSION = 1;

    /**
     * Following are information about my blood sugar model and a list that will use instead
     * of just referencing the DB.
     */
    private BloodSugarModel model;
    private List<BloodSugarModel> results;

    /**
     * Local properties only
     */
    private int totalBloodSugarAround;
    private int minBloodSugarAmount;
    private int maxBloodSugarAmount;
    private int avgBloodSugarAmount;

    /**
     * Since this is a singleton type I will need this to make sure this class is only
     * initiated once.
     */
    private static DatabaseHelper sInstance;


    /**
     * Private Constuctor for my singleton class
     * @param context
     */
    private DatabaseHelper(Context context)
    {
        super(context, DB_NAME, null, DB_VERSION);
        model = new BloodSugarModel();
        results = new ArrayList<>();
        totalBloodSugarAround = 0;
        minBloodSugarAmount = 0;
        maxBloodSugarAmount = 0;
        avgBloodSugarAmount = 0;
    }

    /**
     * Singleton Pattern method : I will only initiate this once which is also why I am using the
     * application context so that I am leaking anything from the activity.
     *
     * @param context context which calls this db class
     * @return return only instance of this class
     */
    public static synchronized DatabaseHelper getInstance(Context context)
    {
        if (sInstance == null)
        {
            sInstance = new DatabaseHelper(context.getApplicationContext());
        }
        return sInstance;
    }

    /**
     * Creates the TABLE for the first time
     * @param db SQLiteDatabase so that it can used to create the tables for the 1st
     */
    @Override
    public void onCreate(SQLiteDatabase db)
    {
        String sql = "CREATE TABLE " + TABLE_NAME
                   + "( " + ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                   + DATE_TESTED + " VARCHAR"
                   + TIME_TESTED + " VARCHAR"
                   + AMOUNT + " INTEGER);";
        db.execSQL(sql);
    }

    /**
     * If upgrade happens then I will simple reset the database
     * @param db Database class called this
     * @param oldVersion oldVersion number
     * @param newVersion newVersion number
     */
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion)
    {
        if (oldVersion == newVersion)
        {
            String sql = "DROP TABLE IF EXIST " + TABLE_NAME;
            db.execSQL(sql);
            onCreate(db);
        }
    }

    /**
     * Purges and resets the database back to its default behavior without any rows added.
     * Also resets how I store the results internally
     */
    public void purgeDB()
    {
        SQLiteDatabase db = getWritableDatabase();
        onUpgrade(db, DB_VERSION, DB_VERSION);

        // Since I am keeping a list internally, i need to keep them synchronized
        results.clear();
        maxBloodSugarAmount = minBloodSugarAmount = avgBloodSugarAmount = totalBloodSugarAround = 0;
    }



    // GET PROPERTIES
    public int getMaxAmount() { return maxBloodSugarAmount; }
    public int getMinAmount() { return minBloodSugarAmount; }
    public int getAvgAmount() { return avgBloodSugarAmount; }
    public int getTotalAmount() { return totalBloodSugarAround; }
    public int getNumberTests() { return results.size(); }
}
