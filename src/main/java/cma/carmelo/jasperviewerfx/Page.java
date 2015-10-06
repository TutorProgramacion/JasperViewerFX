package cma.carmelo.jasperviewerfx;

import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.ReadOnlyBooleanWrapper;
import javafx.beans.property.ReadOnlyIntegerProperty;
import javafx.beans.property.ReadOnlyIntegerWrapper;

/**
 * Created by Carmelo Marín Abrego on 13/9/15.
 */
public class Page {
    private int last, first, actual;

    private final ReadOnlyIntegerWrapper actualPage;
    private final ReadOnlyBooleanWrapper firstPage;
    private final ReadOnlyBooleanWrapper lastPage;

    public ReadOnlyBooleanProperty lastPageProperty() {
        return lastPage.getReadOnlyProperty();
    }

    public Page(int last, int first, int actual) {
        this.last = last;
        this.first = first;
        this.actual = actual;
        this.actualPage = new ReadOnlyIntegerWrapper();
        this.lastPage = new ReadOnlyBooleanWrapper();
        this.firstPage = new ReadOnlyBooleanWrapper();
        this.updateInfoPage(actual);
    }

    private void updateInfoPage(int page){
        actualPage.set(page + 1);
        firstPage.set(page == first);
        lastPage.set(page == last);
        actual = page;
    }

    public Page moveNext() {
        updateInfoPage(actual < last ? actual + 1 : last);
        return this;
    }

    public Page movePrev() {
        updateInfoPage(actual > first ? actual - 1 : first);
        return this;
    }

    public Page moveLast(){
        updateInfoPage(last);
        return this;
    }

    public Page moveFirst(){
        updateInfoPage(first);
        return this;
    }

    public Page MoveTo(int page){
        updateInfoPage(page < first ? first : page > last ? last : page);
        return this;
    }

    public int getLast() {
        return last;
    }

    public int getFirst() {
        return first;
    }

    public int getActual() {
        return actual;
    }

    public int getActualPage() {
        return actualPage.get();
    }

    public ReadOnlyIntegerProperty actualPageProperty() {
        return actualPage.getReadOnlyProperty();
    }

    public boolean getFirstPage() {
        return firstPage.get();
    }

    public ReadOnlyBooleanProperty firstPageProperty() {
        return firstPage.getReadOnlyProperty();
    }

    public boolean getLastPage() {
        return lastPage.get();
    }
}