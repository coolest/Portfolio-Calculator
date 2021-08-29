package projects.me.itemsniper;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.util.EntityUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

public class Prices {
    public static List<Double> getCoinPriceInfo(CloseableHttpClient client, String url){
        HttpGet get = new HttpGet(url);
        try(CloseableHttpResponse response = client.execute(get)){
            String html = EntityUtils.toString(response.getEntity());
            Document doc = Jsoup.parse(html);
            Element priceLabel = null;
            Element percentChangeLabel = null;
            boolean lowered = false;
            try{
                priceLabel = doc.getElementsByClass("priceValue___11gHJ").first();
                percentChangeLabel = doc.getElementsByClass("sc-1v2ivon-0 fiaaIx").first();
                if (percentChangeLabel == null){
                    percentChangeLabel = doc.getElementsByClass("sc-1v2ivon-0 iQVSWO").first();
                    lowered = true;
                }
            }catch(NullPointerException ignore){}
            
            String price = "0.00";
            String percentChange = "0.00";
            if (priceLabel != null && percentChangeLabel != null){
                price = priceLabel.text().replace("$", "");
                percentChange = percentChangeLabel.text().replace("%", "");
            }

            return Arrays.asList(
                Double.parseDouble(price.replace(",", "")),
                Double.parseDouble(percentChange) * (lowered ? -1.00 : 1.00)
            );
        }catch(IOException e){
            e.printStackTrace();
        }

        return Arrays.asList(0.00, 0.00);
    }
}
