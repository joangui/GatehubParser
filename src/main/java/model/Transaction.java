package model;

import java.text.DateFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import static java.lang.Math.abs;

public class Transaction {
    public Date DATE;
    public Type TYPE;
    public Coin COIN;
    public Double EUROS_AMOUNT=0D;
    public Double COIN_AMOUNT=0D;
    public Double COIN_COST=0D;
    public Double FEE=0D;

    public enum Type {
        BUY,SELL,FEE
    }

    public enum Coin {
        XRP,BTC,ETH,BCH
    }



    static DateFormat inputDateFormat = new SimpleDateFormat("MMM dd, yyyy, kk:mm", Locale.ENGLISH);
    DateFormat outputDateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.ENGLISH);



    public Transaction (List<List<String>> operations) throws ParseException {
        List<String> op1 = operations.get(0);
        DATE = inputDateFormat.parse(op1.get(0));

        switch (operations.size())
        {
            //FEE
            case 1:
                TYPE= Type.FEE;
                FEE = new Double(operations.get(0).get(3));
                COIN = Coin.XRP;

                break;

            //EXCHANGE
            case 2:
               processExchange(operations);

                break;

            //EXCHANGE WITH FEE
            case 3:
                processExchange(operations);
                FEE = new Double(operations.get(2).get(3));
                break;
        }
    }

    private void processExchange(List<List<String>> operations) {
        EUROS_AMOUNT = new Double(operations.get(0).get(3));
        TYPE = EUROS_AMOUNT<0?Type.BUY:Type.SELL;
        COIN_AMOUNT = new Double((operations.get(1).get(3)));
        COIN_COST = abs(EUROS_AMOUNT/COIN_AMOUNT);
        COIN= Coin.valueOf(operations.get(1).get(4));
    }



    @Override
    public String toString() {
        String date = outputDateFormat.format(DATE);
        switch (TYPE)
        {
            case FEE:
                return date+","+FEE;
            case BUY:
                return  date+","+TYPE+","+COIN+","+EUROS_AMOUNT+","+COIN_AMOUNT+","+COIN_COST+","+FEE;
            case SELL:
                return  date+","+TYPE+","+COIN+","+EUROS_AMOUNT+","+COIN_AMOUNT+","+COIN_COST+","+FEE;
            default:
                return "";
        }
    }

}
