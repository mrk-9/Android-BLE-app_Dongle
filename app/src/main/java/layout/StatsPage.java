package layout;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.belladati.sdk.dataset.Attribute;
import com.belladati.sdk.filter.Filter;
import com.belladati.sdk.filter.FilterOperation;
import com.belladati.sdk.filter.FilterValue;
import com.belladati.android.views.StackedBarChart;
import com.example.karimt.belladatidongleapp.ConnectBTActivity;
import com.example.karimt.belladatidongleapp.MainActivity;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;


import com.example.karimt.belladatidongleapp.R;


/**
 * Created by KarimT on 22.09.2016.
 */
public class StatsPage extends Fragment {

    private StackedBarChart chart;

    public StatsPage() {
    }

    public static StatsPage newInstance() {
        StatsPage fragment = new StatsPage();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_statistics, container, false);
        super.onCreate(savedInstanceState);

        chart = (StackedBarChart) rootView.findViewById(R.id.chart);

        if (ConnectBTActivity.internetConnection) {
            Filter.MultiValueFilter fUserId, fDate;
            fUserId = FilterOperation.IN.createFilter(getAttribute("L_DRIVER", "32531"));
            fUserId.addValue(new FilterValue("D1654"));
            fDate = FilterOperation.IN.createFilter(getAttribute("L_DATE", "32531"));
            fDate.addValue(new FilterValue(HomePage.selMonth));
            ObjectNode filterNode = new ObjectMapper().createObjectNode();
            filterNode.setAll(fUserId.toJson());
            filterNode.setAll(fDate.toJson());

            //chart.setFilterNode(filterNode);
            chart.setService(MainActivity.service);
            chart.setIdChart("48326-c2SrOhjwkz");
            try {
                chart.createStackedBarChart();
            } catch (Exception e) {
                Log.e("error", e.getMessage());
            }
        } else {
        }

        return rootView;
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

}
