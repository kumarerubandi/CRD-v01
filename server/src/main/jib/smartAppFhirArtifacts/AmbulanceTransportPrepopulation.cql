library NonEmergentAmbulanceTransport
using FHIR version '4.0.0'
include FHIRHelpers version '3.0.0' called FHIRHelpers

codesystem "ICD-10-CM": 'http://hl7.org/fhir/sid/icd-10-cm'
codesystem "LOINC": 'http://loinc.org'
codesystem "HCPCS": 'https://hcpcs.codes/'
codesystem "SNOMED-CT": 'http://snomed.info/sct'
codesystem "CPT": 'https://www.aapc.com/resources/medical-coding/cpt.aspx'
codesystem "NUCCPT":'https://www.nlm.nih.gov/research/umls/sourcereleasedocs/current/NUCCPT/sourcerepresentation.html'

//Reduced mobility
code "Z74.0": 'Z74.0' from "ICD-10-CM"
//Other problems related to care provider dependency
code "Z74.8": 'Z74.8' from "ICD-10-CM"
//Dependence on other enabling machines and devices
code "Z99.8": 'Z99.8' from "ICD-10-CM"
//Dialysis
code "Z99.2": 'Z99.2' from "ICD-10-CM"
//Fall from moving wheelchair
code "V00.811": 'V00.811 ' from "ICD-10-CM"

//Under care of public health physician (finding)
code "305524001": '305524001' from "SNOMED-CT"
// Physician supervision of a hospice patient
code "99377": '99377' from "CPT"

//Physician Assistants & Advanced Practice Nursing Providers; Clinical Nurse Specialist, Home Health
code "364SH0200X" : '364SH0200X' from "NUCCPT"

//Medicare or comparable number 
code "45397-7" : '45397-7' from "LOINC"

//National provider ID
code "45952-9" : '45952-9' from "LOINC"

//CMS certification number
code "46456-0" : '46456-0' from "LOINC"

//Facility Address
code "65833-6" : '65833-6' from "LOINC"

//hcpcs codes for NonEmergentAmbulanceTransport
code "A0426" : 'A0426' from "HCPCS"
code "A0428" : 'A0428' from "HCPCS"

parameter supply_request SupplyRequest
parameter "Measurement Period" Interval<DateTime>

context Patient

define NonEmergentAmbulanceTransportCodes : {'A0426','A0428'}

define NonEmergentAmbulanceTransport:
	singleton from (
  		[Procedure] service
    		where service.code.coding[0].code.value in "NonEmergentAmbulanceTransportCodes"
    		 )
    		
define Today: Today()

define FHIRQueries:
	{'Procedure?code=A0426',
	'Procedure?code=A0428',
	'Practitioner/'+"Practitioner".id.value,
	'Organization/'+"AmbulanceSupplier".id.value
	}

// Beneficiary info
define BeneficiaryNameObject: 
	singleton from (Patient.name name where name.use.value = 'official')

define BeneficiaryName: 
	PatientFirstName + ' '+ PatientMiddleInitial + ' '+ PatientLastName

define PatientLastName: "BeneficiaryNameObject".family.value
define PatientMiddleInitial: GetMiddleInitials("BeneficiaryNameObject")
define PatientFirstName: "BeneficiaryNameObject".given[0].value

define BeneficiaryGender: Patient.gender.value

define BeneficiaryDateOfBirth: Patient.birthDate.value

define BeneficiaryMedicareNumber:
	(singleton from (
  	Patient.identifier identifier
   	 where identifier.system.value = 'http://hl7.org/fhir/sid/us-medicare')).value.value


// Practitioner info
define Practitioner: 
	singleton from (
  		[Practitioner] practitioner
    		where ('Practitioner/' + practitioner.id.value) = "NonEmergentAmbulanceTransport".performer[0].actor.reference.value)
    
define PractitionerNameObject: 
	singleton from ("Practitioner".name)
	
define PractitionerName: 
	//PractitionerFirstName + ' '+ PractitionerMiddleInitial + ' '+ PractitionerLastName
	PractitionerFirstName + ' '+ PractitionerLastName

define PractitionerLastName: "PractitionerNameObject".family.value
define PractitionerMiddleInitial: GetMiddleInitials("PractitionerNameObject")
define PractitionerFirstName: "PractitionerNameObject".given[0].value

define PractitionerNPI: 
	(singleton from (
  		"Practitioner".identifier identifier
    		where identifier.system.value = 'http://hl7.org/fhir/sid/us-npi')).value.value
    
define PractitionerAddressObject: singleton from ("Practitioner".address address where address.use.value = 'home')  

define PractitionerPTAN:
	(singleton from (
  		"Practitioner".identifier identifier
    		where identifier.type.coding[0].system.value = 'http://terminology.hl7.org/CodeSystem/v2-0203' and identifier.type.coding[0].code.value = 'PRN')).value.value

define PractitionerAddress: 
	"PractitionerAddressObject".line[0].value +', ' +"PractitionerAddressObject".city.value +', ' +"PractitionerAddressObject".state.value+', ' +"PractitionerAddressObject".postalCode.value

// Ambulance Supplier info

define SupplyRequestObject:
	singleton from ([SupplyRequest])

define AmbulanceSupplier: 
	singleton from (
  		[Organization] org
    		where ('Organization/' + org.id.value) = "SupplyRequestObject".supplier[0].reference.value)

//define AmbulanceSupplierService: 
	//singleton from (
  		//[HealthcareService] service
    		//where ('HealthcareService/' + service.id.value) = supply_request.supplier[0].reference.value)

//define AmbulanceSupplier: 
	//if exists(AmbulanceSupplierOrg) then AmbulanceSupplierOrg
	//else AmbulanceSupplierService
	
define AmbulanceSupplierName: "AmbulanceSupplier".name.value

define AmbulanceSupplierNPI: (singleton from (
  "AmbulanceSupplier".identifier identifier
    where identifier.system.value = 'http://hl7.org/fhir/sid/us-npi')).value.value

define AmbulanceSupplierPTAN: (singleton from (
  "AmbulanceSupplier".identifier identifier
    where identifier.type.coding[0].system.value = 'http://terminology.hl7.org/CodeSystem/v2-0203' and identifier.type.coding[0].code.value = 'PRN')).value.value
    
define AmbulanceSupplierAddressObject: singleton from ("AmbulanceSupplier".address)  

define AmbulanceSupplierAddress: 
	"AmbulanceSupplierAddressObject".line[0].value +', ' +"AmbulanceSupplierAddressObject".city.value +', ' +"AmbulanceSupplierAddressObject".state.value+', ' +"AmbulanceSupplierAddressObject".postalCode.value

define SubmissionDate: Today

define TransportContraindicated: true

define MedicallyRequired: true

define Dialysis: 
	singleton from ([Condition] c where c.code.coding[0].code.value= 'Z99.2')

define RelevantDiagnoses: 
	{
	if exists("Dialysis")  then 'Dialysis' else 'null',
	if exists("RiskOfFalling") then 'Risk of falling off wheelchair or stretcher while in motion (not related to obesity)' else 'null'
	}

define RiskOfFalling: 
	singleton from ([Condition] c where c.code.coding[0].code.value= 'V00.811')

define LocationFromObject:
	singleton from ([Location] l1 where  ('Location/' + l1.id.value) = "SupplyRequestObject".deliverFrom.reference.value)
	
define LocationToObject:
	singleton from ([Location] l2 where ('Location/' + l2.id.value) = "SupplyRequestObject".deliverTo.reference.value)

define LocationFromAddressObject: singleton from ("LocationFromObject".address)  

define TransportFrom: 
	"LocationFromAddressObject".line[0].value +', ' +"LocationFromAddressObject".city.value +', ' +"LocationFromAddressObject".state.value+', ' +"LocationFromAddressObject".postalCode.value

define LocationToAddressObject: singleton from ("LocationToObject".address)  

define TransportTo: 
	"LocationToAddressObject".line[0].value +', ' +"LocationToAddressObject".city.value +', ' +"LocationToAddressObject".state.value+', ' +"LocationToAddressObject".postalCode.value

define RoundTrip: false

//Requestor= info
define Requestor: 
	singleton from (
  		[Practitioner] p
    		where p.identifier[0].value.value = '1932102951' and p.identifier[0].system.value='http://hl7.org.fhir/sid/us-npi')
    		
define RequestorNameObject: 
	singleton from ("Requestor".name)
	
define RequestorName: 
	//PractitionerFirstName + ' '+ PractitionerMiddleInitial + ' '+ PractitionerLastName
	RequestorFirstName + ' '+ RequestorLastName

define RequestorLastName: "RequestorNameObject".family.value
define RequestorMiddleInitial: GetMiddleInitials("RequestorNameObject")
define RequestorFirstName: "RequestorNameObject".given[0].value

define RequestorTelephone: 
	(singleton from (
  		"Requestor".telecom tele
    		where tele.system.value = 'phone' and tele.use.value='work')).value.value

define function GetMiddleInitials(name FHIR.HumanName):
  Substring(Combine((name.given given return Substring(given.value,0,1)),', '),3)


define function GetProcedure(ProcedureList List<Procedure>, ProcedureCode String):
  ProcedureList P where P.code.coding[0].code.value= ProcedureCode
