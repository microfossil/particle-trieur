package particletrieur.helpers;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;

import java.io.PrintWriter;
import java.io.StringWriter;

public class ExceptionMonitor implements Thread.UncaughtExceptionHandler {

    StringBuilder sb = new StringBuilder();

    IntegerProperty errorCount = new SimpleIntegerProperty(0);
    public int getErrorCount() {
        return errorCount.get();
    }
    public IntegerProperty errorCountProperty() {
        return errorCount;
    }
    public void setErrorCount(int errorCount) {
        this.errorCount.set(errorCount);
    }

    @Override
    public void uncaughtException(Thread t, Throwable e) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        e.printStackTrace(pw);
        sb.append(sw.toString());
        sb.append("\n");
        pw.close();
        setErrorCount(getErrorCount()+1);
        e.printStackTrace();
    }

    public String getLog() {
        return sb.toString();
    }
}
