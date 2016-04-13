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
 * Created by keithyau on 11/7/13.
 */

public class DrupalServicesFile extends DrupalServicesBase implements DrupalServicesResource {


    public DrupalServicesFile(String baseURI, String endpoint) {
        super(baseURI, endpoint);
        this.setResource("file");
    }

    @Override
    public HashMap<String,String> create(BasicNameValuePair[] params) throws Exception {
        return this.httpPostRequest(this.getURI(), params);
    }

    @Override
    public HashMap<String,String> retrieve(int id) throws Exception{
        return this.httpGetRequest(this.getURI() + "/" + id);
    }

    @Override
    public HashMap<String,String> update(int id, BasicNameValuePair[] params) throws Exception{
        return this.httpPutRequest(this.getURI() + "/" + id, params);
    }

    @Override
    public HashMap<String,String> delete(int id) throws Exception {
        return this.httpDeleteRequest(this.getURI() + "/" + id);
    }

    @Override
    public HashMap<String,String> index() throws Exception {
        return this.httpGetRequest(this.getURI());
    }
}