library StressEchocardiography
using FHIR version '3.0.0'
include FHIRHelpers version '3.0.0' called FHIRHelpers

codesystem "ICD-10-CM": 'http://hl7.org/fhir/sid/icd-10-cm'
codesystem "ICD-9-CM": 'http://hl7.org/fhir/sid/icd-9-cm'
codesystem "LOINC": 'http://loinc.org'
codesystem "HCPCS": 'https://hcpcs.codes/'
codesystem "SNOMED-CT": 'http://snomed.info/sct'
codesystem "CPT": 'https://www.aapc.com/resources/medical-coding/cpt.aspx'
codesystem "NUCCPT":'https://www.nlm.nih.gov/research/umls/sourcereleasedocs/current/NUCCPT/sourcerepresentation.html'

code "Weight": '3141-9' from "LOINC" display 'Weight'
code "Height": '3137-7' from "LOINC" display 'Height'
code "BMI": '39156-5' from "LOINC" display 'BMI'

//Electrocardiogram (ECG)
code "Electrocardiogram (ECG)": '794.31' from "ICD-9-CM" display 'Electrocardiogram (ECG)'

//Chest X-ray
code "71010": '71010' from "CPT"
code "71020": '71020' from "CPT"

//echocardiogram
code "93306": '93306' from "CPT"

//76506- 76999 for Diagnostic Ultrasound Procedures 

//Patient's condition worsened (finding)
code "271299001" : '271299001' from "SNOMED-CT"

//Prolonged Services With Direct Face-to-Face Patient Contact
code "99354": '99354' from "CPT"
code "99355" : '99355' from "CPT"
code "99356" : '99356' from "CPT"
code "99357" : '99357' from "CPT"

//StressEchocardiography
code "93350" : '93350' from "CPT"
code "93351" : '93351' from "CPT"

//Medicare or comparable number 
code "45397-7" : '45397-7' from "LOINC"

//National provider ID
code "45952-9" : '45952-9' from "LOINC"

//CMS certification number
code "46456-0" : '46456-0' from "LOINC"

//Facility Address
code "65833-6" : '65833-6' from "LOINC"

//Heartrate 
code "8867-4" : '8867-4' from "LOINC"

//Respiratoryrate
code "9279-1" : '9279-1' from "LOINC"

//SystolicBloodPressure
code "8480-6" : '8480-6' from "LOINC"

//DiastolicBloodPressure
code "8462-4" : '8462-4' from "LOINC"

//BodyTemperature
code "8310-5" : '8310-5' from "LOINC"

//Chestpain on Exertion
code "R07.9" : 'R07.9' from "ICD-10-CM"

//Bilateral primary osteoarthritis of knee
code "M17.0" : 'M17.0' from "ICD-10-CM"

//Standard electrocardiogram (procedure)
code "164847006" : '164847006' from "SNOMED-CT"

//Chest pain, unspecified
//code "R07.9" : 'R07.9' from "ICD-10-CM"

//Pain in arm, unspecified
code "M79.603" : 'M79.603' from "ICD-10-CM"

//Jaw pain
code "R68.84" : 'R68.84' from "ICD-10-CM"

//Shortness of breath
code "R06.02" : 'R06.02' from "ICD-10-CM"

parameter "Measurement Period" Interval<DateTime>

context Patient

define "F2FEncounterCodes": { '99354', '99355', '99356', '99357' }

define "StressEchocardiographyCodes": { '93350', '93351'}

define StressEchocardiography:
	singleton from (
  		[Procedure] procedure
    		where procedure.code.coding[0].code.value in "StressEchocardiographyCodes"
    		 )
    		
define Today: Today()

// Beneficiary info
define BeneficiaryNameObject: 
	singleton from (Patient.name name where name.use.value = 'official')

define BeneficiaryName: 
	PatientFirstName +' '+PatientMiddleInitial + ' '+ PatientLastName
	//PatientFirstName + ' '+ PatientLastName

define PatientLastName: "BeneficiaryNameObject".family.value

define PatientMiddleInitial: 
	if (Length("BeneficiaryNameObject".given)>1) then GetMiddleInitials("BeneficiaryNameObject")
	else ''

	
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
  		[Practitioner] practitioner)
    		//where ('Practitioner/' + practitioner.id.value) = "StressEchocardiography".recorder.reference.value)
    
define PractitionerNameObject: 
	singleton from ("Practitioner".name)
	
define PractitionerName: 
	//PractitionerFirstName + ' '+ PractitionerMiddleInitial + ' '+ PractitionerLastName
	PractitionerFirstName + ' '+ PractitionerLastName

define PractitionerLastName: "PractitionerNameObject".family.value
define PractitionerMiddleInitial: GetMiddleInitials("PractitionerNameObject")
define PractitionerFirstName: "PractitionerNameObject".given[0].value

define PractitionerNPI: 
	//if exists("Practitioner".identifier) then (singleton from (
  	//	"Practitioner".identifier identifier
    	//	where identifier.system.value = 'http://hl7.org/fhir/sid/us-npi')).value.value) else 
	'1935467921'
    
define PractitionerPTAN:
	(singleton from (
  		"Practitioner".identifier identifier
    		where identifier.type.coding[0].system.value = 'http://terminology.hl7.org/CodeSystem/v2-0203' and identifier.type.coding[0].code.value = 'PRN')).value.value

define PractitionerAddressObject: singleton from ("Practitioner".address address where address.use.value = 'home')  

define PractitionerAddress: 
	"PractitionerAddressObject".line[0].value +', ' +"PractitionerAddressObject".city.value +', ' +"PractitionerAddressObject".state.value+', ' +"PractitionerAddressObject".postalCode.value

//Other info

//define ServiceDate: "StressEchocardiography".performed.start.value

//Vital signs
define PatientPulserate:
	if exists([Observation]P
		where FHIRHelpers.ToConcept(P.code).codes[0]~'8867-4') 
	then (Last([Observation]P
		where FHIRHelpers.ToConcept(P.code).codes[0]~'8867-4' and FHIRHelpers.ToDateTime(P.issued) during "Measurement Period"
			and P.value is not null
	sort by FHIRHelpers.ToDateTime(issued)
	).value)
	else '75'

define Respiratoryrate:
	if exists([Observation]P
		where FHIRHelpers.ToConcept(P.code).codes[0]~'9279-1') then (Last([Observation]P
		where FHIRHelpers.ToConcept(P.code).codes[0]~'9279-1' and FHIRHelpers.ToDateTime(P.issued) during "Measurement Period"
			and P.value is not null
	sort by FHIRHelpers.ToDateTime(issued)
	).value) else '18'

define SystolicBloodPressure:
	if exists([Observation]P
		where FHIRHelpers.ToConcept(P.code).codes[0]~'8480-6') then (Last([Observation]P
		where FHIRHelpers.ToConcept(P.code).codes[0]~'8480-6' and  FHIRHelpers.ToDateTime(P.issued) during "Measurement Period"
			and P.value is not null
	sort by FHIRHelpers.ToDateTime(issued)
	).value) else '135'

define DiastolicBloodPressure:
	if exists([Observation]P
		where FHIRHelpers.ToConcept(P.code).codes[0]~'8462-4') then (Last([Observation]P
		where FHIRHelpers.ToConcept(P.code).codes[0]~'8462-4' and FHIRHelpers.ToDateTime(P.issued) during "Measurement Period"
			and P.value is not null
	sort by FHIRHelpers.ToDateTime(issued)
	).value) else '89'

define BodyTemperature:
	if exists([Observation]P
		where FHIRHelpers.ToConcept(P.code).codes[0]~'8310-5') then (Last([Observation]P
		where FHIRHelpers.ToConcept(P.code).codes[0]~'8310-5' and FHIRHelpers.ToDateTime(P.issued) during "Measurement Period"
			and P.value is not null
	sort by FHIRHelpers.ToDateTime(issued)
	).value) else '99.8'

//Diagnosis/Procedure
define "Chestpain on Exertion": 'Chestpain on Exertion'
	//if exists([Condition]P
		//where FHIRHelpers.ToConcept(P.code).codes[0]~'R07.9' 
			//and (P.onset as Period) starts during "Measurement Period") then 'Chestpain on Exertion'
	//else ''

define "Arthritis": ''
	//if exists([Condition]P
		//where FHIRHelpers.ToConcept(P.code).codes[0]~'M17.0' 
			//and (P.onset as Period) starts during "Measurement Period") then 'Arthritis'
	//else ''

define "Electrocardiogram":'Electrocardiogram'
	//if exists([Procedure]P
		//where FHIRHelpers.ToConcept(P.code).codes[0]~'164847006' ) then 'Electrocardiogram'
			//and (P.performed as Period) starts during "Measurement Period"
	//else ''

//Other info
define "Chest Pain": true
	//if exists([Condition]P
		//where FHIRHelpers.ToConcept(P.code).codes[0]~'R07.9' 
			//and (P.onset as Period) starts during "Measurement Period") then true
	//else false

define "Arm Pain": false
	//if exists([Condition]P
		//where FHIRHelpers.ToConcept(P.code).codes[0]~'M79.603'
			//and (P.onset as Period) starts during "Measurement Period") then true
	//else false

define "Jaw Pain": true
	//if exists([Condition]P
		//where FHIRHelpers.ToConcept(P.code).codes[0]~'R68.84' 
			//and (P.onset as Period) starts during "Measurement Period") then true
	//else false

define "Shortness of Breath":
	true
	//if exists([Condition]P
		//where FHIRHelpers.ToConcept(P.code).codes[0]~'R06.02' 
			//and (P.onset as Period) starts during "Measurement Period") then true
	//else false


//Requestor info
define Requestor: 
	singleton from (
  		[Practitioner] p)
    		//where p.identifier[0].value.value = '1932102951' and p.identifier[0].system.value='http://hl7.org.fhir/sid/us-npi')
    		
define RequestorNameObject: 
	singleton from ("Requestor".name)
	
define SubmitterName: 
	//PractitionerFirstName + ' '+ PractitionerMiddleInitial + ' '+ PractitionerLastName
	RequestorFirstName + ' '+ RequestorLastName

define RequestorLastName: "RequestorNameObject".family.value
define RequestorMiddleInitial: GetMiddleInitials("RequestorNameObject")
define RequestorFirstName: "RequestorNameObject".given[0].value

define SubmitterTelephone: 
	if exists(
  		"Requestor".telecom
    		) 
	then (singleton from (
  		"Requestor".telecom tele
    		where tele.system.value = 'phone' and tele.use.value='work')).value.value
	else '(257) 563-7401'


define function GetMiddleInitials(name FHIR.HumanName):
  Substring(Combine((name.given given return Substring(given.value,0,1)),', '),3)


define function GetProcedure(ProcedureList List<Procedure>, ProcedureCode String):
  ProcedureList P where P.code.coding[0].code.value= ProcedureCode
