package projects.me.itemsniper;

import java.awt.Color;
import java.awt.Font;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.swing.GroupLayout;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.WindowConstants;

import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;

public class App {
    private static HashMap<String, Double> info = new HashMap<String, Double>();
    static {
        info.put("litecoin", 33.95);
        info.put("digibyte", 60516.5);
        info.put("dash", 8.492);
        //info.put("origin-protocol", 1509.3);
        //info.put("ethereum", 0.9045);
    }

    public static String getUrl(String coinType){
        return String.format(
            "https://coinmarketcap.com/currencies/%s/",
            coinType
        );
    }
    
    public static String commatize(String str){
        String decimal = str.substring(str.indexOf(".", 0));
        int length = decimal.length();

        str = str.substring(0, str.indexOf(".", 0));
        for (int i = str.length(); i > 3; i-=3)
            str = str.substring(0, i-3) + "," + str.substring(i-3);
        
        return str + decimal.substring(0, length < 3 ? length : 3);
    }

    public static void main (String[] args){
        CloseableHttpClient client = HttpClients.createDefault();
        HashMap<String, JLabel> priceLabels = new HashMap<String, JLabel>();

        JFrame window = new JFrame("Coin Prices");
        window.setSize(900, 300);
        window.setVisible(true);
        window.setResizable(false);

        JPanel panel = new JPanel();
        panel.setBackground(Color.black);

        GroupLayout layout = new GroupLayout(panel);
        GroupLayout.ParallelGroup group = layout.createParallelGroup();
        
        // Initialize the labels
        for (String key : info.keySet()){
            JLabel priceDisplay = new JLabel();
            group.addComponent(priceDisplay);
            group.addComponent(new JLabel("   "));

            priceLabels.put(key, priceDisplay);
            priceDisplay.setFont(new Font(
                Font.SERIF,
                Font.BOLD,
                18
            ));

            priceDisplay.setForeground(Color.WHITE);
        }

        JLabel offsetLabel = new JLabel("                                                                                                                                        ");
        offsetLabel.setFont(new Font(Font.SERIF, Font.BOLD, 20));

        JLabel totalLabel = new JLabel();
        totalLabel.setForeground(Color.GREEN);
        totalLabel.setFont(new Font(
            Font.SERIF,
            Font.BOLD,
            64
        ));

        group.addComponent(offsetLabel);
        group.addComponent(totalLabel);
        layout.setHorizontalGroup(group);
        window.setContentPane(panel);
        window.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

        // Price scraper - gets and updates the prices of the coins, logic behind total investment value.
        while(true){
            double total = 0.00;
            for (String key : info.keySet()){
                JLabel priceLabel = priceLabels.get(key);
                List<Double> priceInfo = Prices.getCoinPriceInfo(client, getUrl(key));
                if (priceInfo != null){
                    Double price = priceInfo.get(0);
                    Double percentChange = priceInfo.get(1);
                    double amount = info.get(key);
                    String styleColor = percentChange == 0 ? "white" : (percentChange < 0 ? "red" : "green");
                    String changeInPrice = percentChange == 0 ? "no-change" : ((percentChange < 0 ? "" : "+") + percentChange + "%");

                    total+=price*amount;
                    priceLabel.setText(
                        String.format("<html> <p>%s = $%s</p>    <p style=\"font-size:12px\"><i>[%s owned]</i></p>    <p style=\"color:%s\">%s</p></html>", 
                        key, commatize(String.valueOf(price)), amount, styleColor, changeInPrice
                    ));
                }else{
                    priceLabel.setText(key + " = NULL");
                }
            }

            totalLabel.setText("<html><p style=\"font-size:16px; color:white\"><i>Portfolio</i></p> $" + commatize(String.valueOf(total)) + "</html>");

            try {
                TimeUnit.SECONDS.sleep(60);
            } catch (Exception ignore) {}
        }
    }
}
