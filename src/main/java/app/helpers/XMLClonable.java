package main.java.app.helpers;

import javax.xml.bind.*;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

public abstract class XMLClonable<T> {

    public T cloneByXML() {
        try {
            JAXBContext context = JAXBContext.newInstance(this.getClass());
            Marshaller marshaller = context.createMarshaller();
            marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
            Unmarshaller unmarshaller = context.createUnmarshaller();

            //Write to memory
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            marshaller.marshal(this, outputStream);
            //Read back from memory
            ByteArrayInputStream inputStream = new ByteArrayInputStream(outputStream.toByteArray());
            T cloned = (T) unmarshaller.unmarshal(inputStream);
            return cloned;
        } catch (JAXBException e) {
            e.printStackTrace();
        }
        return null;
    }
}
