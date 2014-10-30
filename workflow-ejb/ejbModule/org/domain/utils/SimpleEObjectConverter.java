package org.domain.utils;

import java.util.Map;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;

import org.eclipse.emf.ecore.EObject;

public class SimpleEObjectConverter implements Converter {  
  
    public Object getAsObject(FacesContext ctx, UIComponent component, String value) {  
        if (value != null) {  
            return this.getAttributesFrom(component).get(value);  
        }  
        return null;  
    }  
  
    public String getAsString(FacesContext ctx, UIComponent component, Object value) {  
  
        if (value != null  
                && !"".equals(value)) {  
  
            EObject entity = (EObject) value;  
  
            // adiciona item como atributo do componente  
            this.addAttribute(component, entity);  
            return entity.toString();
        }  
  
        return (String) value;  
    }  
  
    protected void addAttribute(UIComponent component, EObject o) {  
        String key = o.toString(); // codigo da empresa como chave neste caso  
        this.getAttributesFrom(component).put(key, o);  
    }  
  
    protected Map<String, Object> getAttributesFrom(UIComponent component) {  
        return component.getAttributes();  
    }  
  
}  