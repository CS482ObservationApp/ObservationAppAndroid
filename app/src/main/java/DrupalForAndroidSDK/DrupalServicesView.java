/**	 ObservationApp, Copyright 2016, University of Prince Edward Island,
 550 University Avenue, C1A4P3,
 Charlottetown, PE, Canada
 *
 * 	 @author Kent Li <zhuoli@upei.ca>
 *
 *   This file is part of ObservationApp.
 *
 *   ObservationApp is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   CycleTracks is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU General Public License for more details.
 *
 *   You should have received a copy of the GNU General Public License
 *   along with CycleTracks.  If not, see <http://www.gnu.org/licenses/>.
 */

package DrupalForAndroidSDK;

import org.apache.http.message.BasicNameValuePair;

import java.util.HashMap;

/**
 * Created by zhuol on 2/25/2016.
 */
public class DrupalServicesView extends DrupalServicesBase  {
    public enum ViewType {
        OBSERVATION_SEARCH,
        NEWEST_OBSERVATION,
        OBSERVATION_RECORD_AUTOCOMPLETE,
        SINGLE_NODE_DETAIL,
        PERSONAL_OBSERVATION
    }

    public DrupalServicesView(String baseURI, String endpoint) {
        super(baseURI, endpoint);
    }

    public HashMap<String,String> retrieve(ViewType viewType, BasicNameValuePair... params) throws Exception{
        switch (viewType){
            case OBSERVATION_SEARCH: setResource("search-mobile");break;
            case NEWEST_OBSERVATION: setResource("newest-observations-mobile");break;
            case OBSERVATION_RECORD_AUTOCOMPLETE: setResource("observation-record-autocomplete-mobile");break;
            case SINGLE_NODE_DETAIL: setResource("single-node-detail-mobile");break;
            case PERSONAL_OBSERVATION: setResource("my-posts");break;
        }
        return httpGetRequest(getURI(),params);
    }
}
