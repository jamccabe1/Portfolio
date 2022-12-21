/* 
Jacob McCabe
CSCI 330
Assignment 2

I used the skeleton file for this.

This program requires access to the Johnson 330 database which contains
stock price data. The program computes the gain or loss from the trading strategy
*/

import java.util.*;
import java.io.FileInputStream;
import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.io.PrintWriter;
import java.lang.Math;


class McCabeAssignment2 {
   
   static class StockData {	   

        private String date;
        private double open;
        private double high;
        private double low;
        private double close;
        private String split;

        public StockData(String d, double o, double h, double l, double c) {
            date  = d;
            open  = o;
            high  = h;
            low   = l;
            close = c;
            split = null;
        }
    }
   
    static Connection conn;
    static final String prompt = "Enter ticker symbol [start/end dates]: ";

    public static void main(String[] args) throws Exception {
        String paramsFile = "readerparams.txt";
        if (args.length >= 1) {
           paramsFile = args[0];
        }
        
        Properties connectprops = new Properties();
        connectprops.load(new FileInputStream(paramsFile));
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            String dburl = connectprops.getProperty("dburl");
            String username = connectprops.getProperty("user");
            conn = DriverManager.getConnection(dburl, connectprops);
            System.out.printf("Database connection %s %s established.%n", dburl, username);
            
            Scanner in = new Scanner(System.in);
            System.out.print(prompt);
            String input = in.nextLine().trim();
            
            while (input.length() > 0) {
                String[] params = input.split("\\s+");
                String ticker = params[0];
                String startdate = null, enddate = null;
                if (getName(ticker)) {
                    if (params.length >= 3) {
                        startdate = params[1];
                        enddate = params[2];
                    }               
                   Deque<StockData> data = getStockData(ticker, startdate, enddate);
                   System.out.println();
                   System.out.println("Executing investment strategy");
                   doStrategy(ticker, data);
                } 
               
                System.out.println();
                System.out.print(prompt);
                input = in.nextLine().trim();
            }
            System.out.println("Connection to database closed.");
            conn.close();
            in.close();
        } catch (SQLException ex) {
            System.out.printf("SQLException: %s%nSQLState: %s%nVendorError: %s%n",
                              ex.getMessage(), ex.getSQLState(), ex.getErrorCode());
        }
   }
   
   /* Get Name
   Executes a prepared statement to get the company name corresponding
   to the user specified ticker (e.g., INTC to Intel Corp.).
   */
   static boolean getName(String ticker) throws SQLException { 
        PreparedStatement pstmt = conn.prepareStatement(
            " select Name " +
            " from company " +
            " where Ticker = ? ");
        pstmt.setString(1, ticker);

        ResultSet rs = pstmt.executeQuery();
        boolean bool = false;
        if(rs.next()) {
            bool = true;
            System.out.println(rs.getString(1));
        } else {
            System.out.println(ticker + " not found in database.");
        }
        pstmt.close();
        return bool;
   }

    /*Get Stock Data
    Gets stock data for a user specified ticker, determines when stock splits happened
    and prints the split information.
    When dates are unspecified by the user, all data points for that company are used.
    */
    static Deque<StockData> getStockData(String ticker, String start, String end) throws SQLException {	  

        PreparedStatement pstmt = null;
        if (start != null & end != null) {  
            pstmt = conn.prepareStatement(
               "select TransDate, OpenPrice, HighPrice, LowPrice, ClosePrice " +
               " from pricevolume " +
               " where Ticker = ? and TransDate between ? and ? " +
               " order by TransDate desc ");
            pstmt.setString(1, ticker);
            pstmt.setString(2, start);
            pstmt.setString(3, end);
        }
        else {
            pstmt = conn.prepareStatement(
            "select TransDate, OpenPrice, HighPrice, LowPrice, ClosePrice " +
            " from pricevolume " +
            " where Ticker = ? " +
            " order by TransDate desc ");
            pstmt.setString(1, ticker);
        }
        ResultSet rs = pstmt.executeQuery();

        Deque<StockData> result = new ArrayDeque<StockData>();
        double factor = 1.0;
        int count = 0;
        
        while (rs.next()) {
            String date = rs.getString(1);
            double open = Double.parseDouble(rs.getString(2).trim());
            double high = Double.parseDouble(rs.getString(3).trim());
            double low = Double.parseDouble(rs.getString(4).trim());
            double close = Double.parseDouble(rs.getString(5).trim());

            StockData stock = new StockData(date, open, high, low, close);
            stock = adjustSplit(stock, factor);
            if (result.size() >= 1) {
                if (Math.abs((stock.close /result.getFirst().open) - 2) < 0.2) {
                    stock.split = "2:1";
                    count++;
                    System.out.printf("%s split on %s  %.2f --> %.2f%n", 
                            stock.split, stock.date, factor*stock.close, factor*result.getFirst().open);
                    factor = factor * 2.0;
                    stock = adjustSplit(stock, 2.0);    
                } 
                else if (Math.abs((stock.close / result.getFirst().open) - 3) < 0.3) {
                    stock.split = "3:1";
                    count++;
                    System.out.printf("%s split on %s  %.2f --> %.2f%n", 
                            stock.split, stock.date, factor*stock.close, factor*result.getFirst().open);
                    factor = factor * 3.0;
                    stock = adjustSplit(stock, 3.0);
                } 
                else if (Math.abs((stock.close / result.getFirst().open) - 1.5) < 0.15) {
                    stock.split = "3:2";
                    count++;
                    System.out.printf("%s split on %s  %.2f --> %.2f%n", 
                            stock.split, stock.date, factor*stock.close, factor*result.getFirst().open);
                    factor = factor * 1.5;
                    stock = adjustSplit(stock, 1.5);
                } 
            }
            result.addFirst(stock);
        }
        System.out.println(count + " splits in " + result.size() + " trading days");
        pstmt.close();
        return result;
    }
   
    /* Do Strategy
    Uses specific trading rules to determine how much money could have been made in that time.
    Prints the information about transactions.
    Trading only done when there are more than 50 days. 
    */
    static void doStrategy(String ticker, Deque<StockData> data) {

        double cash = 0.0;
        int shares = 0;
        int transactions = 0;
        if (data.size() >= 51) {
            int len = data.size() - 50;
            Deque<StockData> rmvd = new ArrayDeque<StockData>();

            //make first moving average
            double total = 0.0;
            for (int i = 0; i < 50; i++) {
                rmvd.addFirst(data.pop());
                total += rmvd.getFirst().close;
            }
            double movAvg = total / 50;

            //proceed through all avg's trading according to rules
            for (int i = 0; i < len-1; i++) {
                StockData curDay = data.pop();
                //buying criteria
                if (curDay.close < movAvg & curDay.close / curDay.open < 0.97000001) {
                    shares += 100;
                    cash -= 8.0;
                    cash -= 100 * data.getFirst().open;
                    transactions++;
                }//selling criteria
                else if (shares >= 100 & curDay.open >= movAvg & curDay.open/rmvd.getFirst().close > 1.00999999) {
                    shares -= 100;
                    cash -= 8.0;
                    cash += 50*(curDay.open+curDay.close);
                    transactions++;
                }
                rmvd.addFirst(curDay);
                //update moving average for next iteration
                total = total - rmvd.getLast().close + rmvd.getFirst().close;
                movAvg = total / 50;
                rmvd.removeLast();
            }
            cash += shares * data.getFirst().open;
        }
        System.out.printf("Transactions executed: %d\n", transactions);
        System.out.printf("Net cash: %.2f\n", cash);
   }

    /** Adjust Split Factor
     * Takes a StockData and divides all the price info by the factor provided.
     */
    public static StockData adjustSplit(StockData stock, double factor) {
        stock.open = stock.open / factor;
        stock.high = stock.high / factor;
        stock.low = stock.low / factor;
        stock.close = stock.close / factor;
        return stock;
    }
}