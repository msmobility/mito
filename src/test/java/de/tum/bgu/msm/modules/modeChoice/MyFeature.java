package de.tum.bgu.msm.modules.modeChoice;

import org.opengis.feature.GeometryAttribute;
import org.opengis.feature.IllegalAttributeException;
import org.opengis.feature.Property;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.AttributeDescriptor;
import org.opengis.feature.type.Name;
import org.opengis.filter.identity.FeatureId;
import org.opengis.geometry.BoundingBox;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

class MyFeature implements SimpleFeature {

    Map<String, Object> attributes = new HashMap<>();

    MyFeature(boolean isMunich){
        if (isMunich){
            attributes.put("AGS", "9162000");
        } else {
            attributes.put("AGS", "0");
        }
    }

    @Override
    public String getID() {
        return null;
    }

    @Override
    public AttributeDescriptor getDescriptor() {
        return null;
    }

    @Override
    public Name getName() {
        return null;
    }

    @Override
    public boolean isNillable() {
        return false;
    }

    @Override
    public Map<Object, Object> getUserData() {
        return null;
    }

    @Override
    public SimpleFeatureType getType() {
        return null;
    }

    @Override
    public void setValue(Collection<Property> values) {

    }

    @Override
    public Collection<? extends Property> getValue() {
        return null;
    }

    @Override
    public void setValue(Object newValue) {

    }

    @Override
    public Collection<Property> getProperties(Name name) {
        return null;
    }

    @Override
    public Property getProperty(Name name) {
        return null;
    }

    @Override
    public Collection<Property> getProperties(String name) {
        return null;
    }

    @Override
    public Collection<Property> getProperties() {
        return null;
    }

    @Override
    public Property getProperty(String name) {
        return null;
    }

    @Override
    public void validate() throws IllegalAttributeException {

    }

    @Override
    public FeatureId getIdentifier() {
        return null;
    }

    @Override
    public BoundingBox getBounds() {
        return null;
    }

    @Override
    public GeometryAttribute getDefaultGeometryProperty() {
        return null;
    }

    @Override
    public void setDefaultGeometryProperty(GeometryAttribute geometryAttribute) {

    }

    @Override
    public SimpleFeatureType getFeatureType() {
        return null;
    }

    @Override
    public List<Object> getAttributes() {
        return null;
    }

    @Override
    public void setAttributes(List<Object> values) {

    }

    @Override
    public void setAttributes(Object[] values) {

    }

    @Override
    public Object getAttribute(String name) {
        return attributes.get(name);
    }

    @Override
    public void setAttribute(String name, Object value) {

    }

    @Override
    public Object getAttribute(Name name) {
        return null;
    }

    @Override
    public void setAttribute(Name name, Object value) {

    }

    @Override
    public Object getAttribute(int index) throws IndexOutOfBoundsException {
        return null;
    }

    @Override
    public void setAttribute(int index, Object value) throws IndexOutOfBoundsException {

    }

    @Override
    public int getAttributeCount() {
        return 0;
    }

    @Override
    public Object getDefaultGeometry() {
        return null;
    }

    @Override
    public void setDefaultGeometry(Object geometry) {

    }
}
