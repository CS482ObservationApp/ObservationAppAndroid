package Model;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectStreamException;
import java.io.Serializable;

/**
 * Created by zhuol on 4/1/2016.
 */
public class SerializableNameValuePair implements NameValuePair, Serializable {
    private BasicNameValuePair nvp;

    public SerializableNameValuePair(String name, String value) {
        nvp = new BasicNameValuePair(name, value);
    }

    @Override
    public String getName() {
        return nvp.getName();
    }

    @Override
    public String getValue() {
        return nvp.getValue();
    }

    // serialization support

    private static final long serialVersionUID = 1L;

    private void writeObject(ObjectOutputStream out) throws IOException {
        out.writeObject(nvp.getName());
        out.writeObject(nvp.getValue());
    }

    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        nvp = new BasicNameValuePair((String)in.readObject(), (String)in.readObject());
    }

    private void readObjectNoData() throws ObjectStreamException {
        // nothing to do
    }
}
