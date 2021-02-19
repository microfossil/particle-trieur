package main.java.app.helpers;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.InvocationTargetException;

public class BeanCopyService {

    public static void copy(Object from, Object to, Class cls) throws IntrospectionException, IllegalAccessException, InvocationTargetException {
        BeanInfo info = Introspector.getBeanInfo(cls);
        for (PropertyDescriptor propertyDesc : info.getPropertyDescriptors()) {
            if (propertyDesc.getWriteMethod() != null) {
                Object fromValue = propertyDesc.getReadMethod().invoke(from);
                //System.out.println(propertyDesc.getWriteMethod());
                //System.out.println(fromValue);
                propertyDesc.getWriteMethod().invoke(to, fromValue);
            }
        }
    }
}
