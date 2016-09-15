package org.gtri.fhir.api.vistaex.resource.impl;

/**
 * Created by es130 on 9/15/2016.
 */
public enum VistaExResourceType {
    ALLERGY_INTOLLERANCE ("Allergy Intollerance"),
    CONDITION ("Condition"),
    ENCOUNTER ("Encounter"),
    MEDICATION_ADMINISTRATION ("Medication Administration"),
    MEDICATION_ORDER ("Medication Order"),
    OBSERVATION ("Observation"),
    PATIENT ("Patient"),
    PROCEDURE ("Procedure");

    private String resourceName;

    VistaExResourceType(String name){
        resourceName = name;
    }

    public String getName(){
        return resourceName;
    }

}
