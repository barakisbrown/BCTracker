package com.lokislayer.bst.DB;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
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
        // initialize local properties
        model = new BloodSugarModel();
        totalBloodSugarAround = 0;
        minBloodSugarAmount = 0;
        maxBloodSugarAmount = 0;
        avgBloodSugarAmount = 0;
        // Sync db and internal list
        results = getAllResults();
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
     * Determines if the TABLE_NAME exists or not
     * @return true if TABLE_NAME exist, false otherwise
     */
    private boolean isEmpty()
    {
        SQLiteDatabase db = getReadableDatabase();

        String query = "SELECT DISTINCT tbl_name FROM sqlite_master WHERE tbl_name = '"
                     + TABLE_NAME + "'";

        Cursor cursor = db.rawQuery(query,null);

        if (cursor != null)
        {
            if (cursor.getCount() > 0)
            {
                cursor.close();
                return true;
            }
            cursor.close();
        }

        return false;


    }


    /**
     * Loads the internal list of all readings from the backend
     * @return list of all test readings
     */
    private List<BloodSugarModel> getAllResults()
    {
        List<BloodSugarModel> tmpReadings = new ArrayList<>();

        if (!isEmpty())
        {
            SQLiteDatabase db = getReadableDatabase();
            String sql = "SELECT * FROM " + TABLE_NAME;
            Cursor c = db.rawQuery(sql,null);

            if (c.getCount() == 0) return null;

            c.moveToFirst();
            do
            {
                String date = c.getString(c.getColumnIndex(DATE_TESTED));
                String time = c.getString(c.getColumnIndex(TIME_TESTED));
                int amount = c.getInt(c.getColumnIndex(AMOUNT));

                totalBloodSugarAround += amount;
                if (minBloodSugarAmount == 0 && maxBloodSugarAmount == 0)
                {
                    minBloodSugarAmount = maxBloodSugarAmount = 0;
                }
                if (amount > maxBloodSugarAmount)
                    maxBloodSugarAmount = amount;
                if (amount < minBloodSugarAmount)
                    minBloodSugarAmount = amount;

                model = new BloodSugarModel();
                model.setAmount(amount);
                model.setDateTested(date);
                model.setTimeTested(time);
                results.add(model);
            }while(c.moveToNext());

            avgBloodSugarAmount = totalBloodSugarAround / results.size();
        }

        return results;
    }

    /**
     * Inserts a new reading to the backend.
     * @param model test reading that will be stored in the backend
     * @return true if it was done successfully
     */
    public boolean insertReadings(BloodSugarModel model)
    {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put(DATE_TESTED, model.getDateTested());
        values.put(TIME_TESTED, model.getTimeTested());
        values.put(AMOUNT, model.getAmount());

        long result = db.insert(TABLE_NAME,null,values);
        db.close();

        totalBloodSugarAround += model.getAmount();
        avgBloodSugarAmount = totalBloodSugarAround / results.size();
        if (results.size() == 1)
        {
            maxBloodSugarAmount = minBloodSugarAmount = model.getAmount();
        }
        else
        {
            if (model.getAmount() > maxBloodSugarAmount)
                minBloodSugarAmount = model.getAmount();
        }

        return result > 0;
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
