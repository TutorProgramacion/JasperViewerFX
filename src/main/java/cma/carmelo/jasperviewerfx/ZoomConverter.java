package cma.carmelo.jasperviewerfx;

import javafx.util.StringConverter;

/**
 * Created by Carmelo Marín Abrego on 13/9/15.
 */
public class ZoomConverter extends StringConverter<Integer> {

    @Override
    public String toString(Integer object) {
        return object.toString() + " %";
    }

    @Override
    public Integer fromString(String string) {
        String zoom = string.split("%")[0];
        if(zoom.isEmpty()){
            return 100;
        }else {
            try {
                return Integer.parseInt(zoom.trim());
            }catch (NumberFormatException nfe){
                return 100;
            }
        }
    }
}
