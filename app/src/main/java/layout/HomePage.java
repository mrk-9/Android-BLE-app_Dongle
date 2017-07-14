package layout;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.ViewFlipper;

import com.belladati.android.views.Table;
import com.belladati.httpclientandroidlib.client.utils.URIBuilder;
import com.belladati.sdk.BellaDatiService;
import com.belladati.sdk.dataset.Attribute;
import com.belladati.sdk.filter.Filter;
import com.belladati.sdk.filter.FilterOperation;
import com.belladati.sdk.filter.FilterValue;
import com.example.karimt.belladatidongleapp.AsyncCreateTable;
import com.example.karimt.belladatidongleapp.ConnectBTActivity;
import com.example.karimt.belladatidongleapp.MainActivity;
import com.example.karimt.belladatidongleapp.R;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.net.URISyntaxException;
import java.util.ArrayList;

/**
 * Created by KarimT on 22.09.2016.
 */
public class HomePage extends Fragment {
    public static BellaDatiService service;
    float initialY;
    JsonNode jnDates=null;
    private Table table;
    ArrayList<String> dispMonths = new ArrayList<String>();
    ArrayList<String> valueMonths = new ArrayList<String>();
    private ViewFlipper flip;
    public static String selMonth;

    public HomePage() {
    }

    public static HomePage newInstance() {
        HomePage fragment = new HomePage();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        /*try {
            if (ConnectBTActivity.internetConnection) {
                jnDates = getAvailableDates();
            }
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }*/

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_home, container, false);
        super.onCreate(savedInstanceState);
        table = (Table) rootView.findViewById(R.id.tableJson);
        TextView tvHome = (TextView) rootView.findViewById(R.id.tvHome);

        if (!ConnectBTActivity.internetConnection) {
            tvHome.setText(getString(R.string.without_internet));
        } else if(!MainActivity.savedDongleName.equals("")) {
            if(jnDates!=null) {
                JsonNode dates = jnDates.findPath("body");
                for (int d = 0; d < dates.size(); d++) {
                    valueMonths.add(dates.get(d).get(1).findPath("value").asText());
                    dispMonths.add(dates.get(d).get(2).findPath("value").asText());
                    //chinese version
                /*valueMonths.add(dates.get(d).get(1).findPath("value").asText());
                dispMonths.add(dates.get(d).get(3).findPath("value").asText());*/
                }
                selMonth = valueMonths.get(0);

                flip = (ViewFlipper) rootView.findViewById(R.id.flipperMonths);
                flip.setBackgroundResource(R.color.gray);
                if (flip.getChildCount() == 0) {
                    for (int i = 0; i < dispMonths.size(); i++) {
                        TextView textView = new TextView(getActivity());
                        textView.setText(dispMonths.get(i));
                        textView.setTextSize(20);
                        textView.setTextColor(Color.BLACK);
                        textView.setGravity(Gravity.CENTER);
                        flip.addView(textView);
                    }
                }
                flip.setOnTouchListener(new FlipperListener());
            }

            /*Filter.MultiValueFilter fUserId, fDate;

            fUserId = FilterOperation.IN.createFilter(getAttribute("L_SENSOR_ID", "32949"));
            fUserId.addValue(new FilterValue(ConnectBTActivity.dongleName+" - Summary"));

            *//*fDate = FilterOperation.IN.createFilter(getAttribute("L_DATE", "32531"));
            fDate.addValue(new FilterValue(selMonth));*//*

            ObjectNode filterNode = new ObjectMapper().createObjectNode();
            filterNode.setAll(fUserId.toJson());
            //filterNode.setAll(fDate.toJson());
            table.setHeadColumnStyle(R.drawable.cell_shape_d);
            table.setFilterNode(filterNode);
            table.setIdTable("48326-I4UWT4Fx3o");
            table.setService(MainActivity.service);
            try {
                table.createTable();
            } catch (Exception e) {
                e.printStackTrace();
            }*/
            AsyncCreateTable myTask=new AsyncCreateTable();
            try {
                myTask.execute(table);
            }
            catch (Exception e)
            {

            }
            tvHome.setText("Statistics");

        }
        else
        {
            tvHome.setText("First you have to connect to Dongle!");
        }

        return rootView;
    }

    //listener for Flipper
    private class FlipperListener implements View.OnTouchListener {
        public boolean onTouch(View v, MotionEvent event) {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    // Getting intitial by event action down
                    initialY = event.getY();
                    break;

                case MotionEvent.ACTION_UP:
                    // On action up the flipper will start and showing next item
                    float finalY = event.getY();
                    if (initialY > finalY) {
                        if (flip.getDisplayedChild() == flip.getChildCount()) {
                            break;
                        }
                        // Flip show next will show next item
                        flip.setInAnimation(getActivity(), R.anim.down_enter);
                        flip.setOutAnimation(getActivity(), R.anim.up_out);
                        flip.showNext();
                        selMonth = valueMonths.get(flip.getDisplayedChild());
                        table.removeAllViewsInLayout();
                        refreshTable();

                    } else {

                        // If flip has no items more then it will display previous item
                        if (flip.getDisplayedChild() == 0) {
                            flip.setDisplayedChild(flip.getChildCount() - 1);
                            flip.setInAnimation(getActivity(), R.anim.up_enter);
                            flip.setOutAnimation(getActivity(), R.anim.down_out);
                            selMonth = valueMonths.get(flip.getDisplayedChild());
                            table.removeAllViewsInLayout();
                            refreshTable();

                        } else {
                            flip.setInAnimation(getActivity(), R.anim.up_enter);
                            flip.setOutAnimation(getActivity(), R.anim.down_out);
                            flip.showPrevious();
                            selMonth = valueMonths.get(flip.getDisplayedChild());
                            table.removeAllViewsInLayout();
                            refreshTable();
                        }
                    }
                    break;
            }
            return true;
        }
    }

    //get attribute for filter
    private Attribute getAttribute(String filter, String idDataset) {
        for (Attribute aa : MainActivity.service.loadDataSet(idDataset).getAttributes()) {
            if (filter.equalsIgnoreCase(aa.getCode())) {
                return aa;
            }
        }
        return null;
    }

    //get available months from dataset for current driver
    private JsonNode getAvailableDates() throws URISyntaxException {
        Filter.MultiValueFilter fUserId;

        fUserId = FilterOperation.IN.createFilter(getAttribute("L_DRIVER", "32531"));
        fUserId.addValue(new FilterValue("D1654"));

        ObjectNode filterNode = new ObjectMapper().createObjectNode();
        filterNode.setAll(fUserId.toJson());
        ObjectNode drilldownNode = new ObjectMapper().createObjectNode();
        drilldownNode.put("drilldown", filterNode);

        URIBuilder builder = new URIBuilder("api/reports/views/47892-HzdQQnMdJT/table/json");
        builder.addParameter("filter", drilldownNode.toString());
        JsonNode jn = MainActivity.wrapper.loadJson(builder.toString());
        return jn;
    }

    //reset table depend on selected month
    private void refreshTable() {
        /*Filter.MultiValueFilter fUserId, fDate;

        fUserId = FilterOperation.IN.createFilter(getAttribute("L_SENSOR_ID", "32949"));
        fUserId.addValue(new FilterValue(ConnectBTActivity.dongleName+" - Summary"));

        *//*fDate = FilterOperation.IN.createFilter(getAttribute("L_DATE", "32531"));
        fDate.addValue(new FilterValue(selMonth));*//*

        ObjectNode filterNode = new ObjectMapper().createObjectNode();
        filterNode.setAll(fUserId.toJson());
        //filterNode.setAll(fDate.toJson());
        table.setFilterNode(filterNode);

        try {

            table.createTable();
        } catch (Exception e) {
            e.printStackTrace();
        }*/
        AsyncCreateTable myTask=new AsyncCreateTable();
        try {
            myTask.execute(table);
        }
        catch (Exception e)
        {

        }
    }
}
