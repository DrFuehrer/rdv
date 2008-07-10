//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.0.5-b02-fcs 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2007.11.28 at 03:46:45 PM GMT 
//


package org.nees.data;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlType;
import javax.xml.datatype.XMLGregorianCalendar;


/**
 * <p>Java class for SensorAttributes complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="SensorAttributes">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence minOccurs="0">
 *         &lt;element name="attributeId" type="{}IDnumber"/>
 *         &lt;element name="intValue" type="{http://www.w3.org/2001/XMLSchema}int"/>
 *         &lt;element name="numValue" type="{}Double"/>
 *         &lt;element name="stringValue" type="{}String512"/>
 *         &lt;element name="dateValue" type="{}Date"/>
 *         &lt;element name="note" type="{}String512"/>
 *         &lt;element name="unitId" type="{}IDnumber"/>
 *         &lt;element name="groupValue" type="{http://www.w3.org/2001/XMLSchema}int"/>
 *         &lt;element name="pageCount" type="{}SmallInt"/>
 *       &lt;/sequence>
 *       &lt;attribute name="sensorId" use="required" type="{}IDnumber" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "SensorAttributes", propOrder = {
    "attributeId",
    "intValue",
    "numValue",
    "stringValue",
    "dateValue",
    "note",
    "unitId",
    "groupValue",
    "pageCount"
})
public class SensorAttributes {

    protected Integer attributeId;
    @XmlElementRef(name = "intValue", type = JAXBElement.class)
    protected JAXBElement<Integer> intValue;
    @XmlElementRef(name = "numValue", type = JAXBElement.class)
    protected JAXBElement<Double> numValue;
    @XmlElementRef(name = "stringValue", type = JAXBElement.class)
    protected JAXBElement<String> stringValue;
    @XmlElementRef(name = "dateValue", type = JAXBElement.class)
    protected JAXBElement<XMLGregorianCalendar> dateValue;
    @XmlElementRef(name = "note", type = JAXBElement.class)
    protected JAXBElement<String> note;
    @XmlElementRef(name = "unitId", type = JAXBElement.class)
    protected JAXBElement<Integer> unitId;
    @XmlElementRef(name = "groupValue", type = JAXBElement.class)
    protected JAXBElement<Integer> groupValue;
    @XmlElementRef(name = "pageCount", type = JAXBElement.class)
    protected JAXBElement<Integer> pageCount;
    @XmlAttribute(required = true)
    protected int sensorId;

    /**
     * Gets the value of the attributeId property.
     * 
     * @return
     *     possible object is
     *     {@link Integer }
     *     
     */
    public Integer getAttributeId() {
        return attributeId;
    }

    /**
     * Sets the value of the attributeId property.
     * 
     * @param value
     *     allowed object is
     *     {@link Integer }
     *     
     */
    public void setAttributeId(Integer value) {
        this.attributeId = value;
    }

    /**
     * Gets the value of the intValue property.
     * 
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link Integer }{@code >}
     *     
     */
    public JAXBElement<Integer> getIntValue() {
        return intValue;
    }

    /**
     * Sets the value of the intValue property.
     * 
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link Integer }{@code >}
     *     
     */
    public void setIntValue(JAXBElement<Integer> value) {
        this.intValue = ((JAXBElement<Integer> ) value);
    }

    /**
     * Gets the value of the numValue property.
     * 
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link Double }{@code >}
     *     
     */
    public JAXBElement<Double> getNumValue() {
        return numValue;
    }

    /**
     * Sets the value of the numValue property.
     * 
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link Double }{@code >}
     *     
     */
    public void setNumValue(JAXBElement<Double> value) {
        this.numValue = ((JAXBElement<Double> ) value);
    }

    /**
     * Gets the value of the stringValue property.
     * 
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *     
     */
    public JAXBElement<String> getStringValue() {
        return stringValue;
    }

    /**
     * Sets the value of the stringValue property.
     * 
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *     
     */
    public void setStringValue(JAXBElement<String> value) {
        this.stringValue = ((JAXBElement<String> ) value);
    }

    /**
     * Gets the value of the dateValue property.
     * 
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link XMLGregorianCalendar }{@code >}
     *     
     */
    public JAXBElement<XMLGregorianCalendar> getDateValue() {
        return dateValue;
    }

    /**
     * Sets the value of the dateValue property.
     * 
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link XMLGregorianCalendar }{@code >}
     *     
     */
    public void setDateValue(JAXBElement<XMLGregorianCalendar> value) {
        this.dateValue = ((JAXBElement<XMLGregorianCalendar> ) value);
    }

    /**
     * Gets the value of the note property.
     * 
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *     
     */
    public JAXBElement<String> getNote() {
        return note;
    }

    /**
     * Sets the value of the note property.
     * 
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *     
     */
    public void setNote(JAXBElement<String> value) {
        this.note = ((JAXBElement<String> ) value);
    }

    /**
     * Gets the value of the unitId property.
     * 
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link Integer }{@code >}
     *     
     */
    public JAXBElement<Integer> getUnitId() {
        return unitId;
    }

    /**
     * Sets the value of the unitId property.
     * 
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link Integer }{@code >}
     *     
     */
    public void setUnitId(JAXBElement<Integer> value) {
        this.unitId = ((JAXBElement<Integer> ) value);
    }

    /**
     * Gets the value of the groupValue property.
     * 
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link Integer }{@code >}
     *     
     */
    public JAXBElement<Integer> getGroupValue() {
        return groupValue;
    }

    /**
     * Sets the value of the groupValue property.
     * 
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link Integer }{@code >}
     *     
     */
    public void setGroupValue(JAXBElement<Integer> value) {
        this.groupValue = ((JAXBElement<Integer> ) value);
    }

    /**
     * Gets the value of the pageCount property.
     * 
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link Integer }{@code >}
     *     
     */
    public JAXBElement<Integer> getPageCount() {
        return pageCount;
    }

    /**
     * Sets the value of the pageCount property.
     * 
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link Integer }{@code >}
     *     
     */
    public void setPageCount(JAXBElement<Integer> value) {
        this.pageCount = ((JAXBElement<Integer> ) value);
    }

    /**
     * Gets the value of the sensorId property.
     * 
     */
    public int getSensorId() {
        return sensorId;
    }

    /**
     * Sets the value of the sensorId property.
     * 
     */
    public void setSensorId(int value) {
        this.sensorId = value;
    }

}
