package org.processmining.plugins.log.exporting;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Collection;
import java.util.Date;
import java.util.ListIterator;

import org.apache.commons.lang3.time.FastDateFormat;
import org.deckfour.xes.classification.XEventAttributeClassifier;
import org.deckfour.xes.classification.XEventClassifier;
import org.deckfour.xes.extension.XExtension;
import org.deckfour.xes.logging.XLogging;
import org.deckfour.xes.model.XAttribute;
import org.deckfour.xes.model.XAttributeBoolean;
import org.deckfour.xes.model.XAttributeContainer;
import org.deckfour.xes.model.XAttributeContinuous;
import org.deckfour.xes.model.XAttributeDiscrete;
import org.deckfour.xes.model.XAttributeList;
import org.deckfour.xes.model.XAttributeTimestamp;
import org.deckfour.xes.model.XEvent;
import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XTrace;
import org.deckfour.xes.out.XSerializer;
import org.deckfour.xes.util.XRuntimeUtils;

import com.jsoniter.output.JsonStream;

/**
 * XES serialization to JXES including all t
import com.google.gson.JsonObject;race/event attributes.
 *
 * @author Hossameldin Khalifa
 *
 */
public final class JxesJsoniterSerializer implements XSerializer {

	private final FastDateFormat dateFormat = FastDateFormat.getInstance("yyyy/MM/dd HH:mm:ss.SSS");

	
	/*
	 * (non-Javadoc)
	 *
	 * @see org.deckfour.xes.out.XesSerializer#getDescription()
	 */
	public String getDescription() {
		return "XES JXES Serialization";
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.deckfour.xes.out.XesSerializer#getName()
	 */
	public String getName() {
		return "XES JXES";
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.deckfour.xes.out.XesSerializer#getAuthor()
	 */
	public String getAuthor() {
		return "Hossameldin Khalifa";
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.deckfour.xes.out.XesSerializer#getSuffices()
	 */
	public String[] getSuffices() {
		return new String[] { "json" };
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see
	 * org.deckfour.xes.out.XesSerializer#serialize(org.deckfour.xes.model.XLog,
	 * java.io.OutputStream)
	 */
	public void serialize(XLog log, OutputStream out) throws IOException {
		XLogging.log("start serializing log to .json (Jsoniter) ", XLogging.Importance.DEBUG);
		long start = System.currentTimeMillis();
		JsonStream writer = new JsonStream(out, 100000000);

//		PrintStream err = new PrintStream(new java.io.OutputStream(){
//			public void write(int b) throws IOException {
//
//			}});
//		System.addErr(err);
		
		

		//begin json output
		writer.writeObjectStart();
		
		
		// add json key:
		writer.writeObjectField("log-properties");
		
		//begin log-attrs object 
		writer.writeObjectStart();
		
		//add xes version and features
		writer.writeObjectField("xes.version");
		writer.writeVal(XRuntimeUtils.XES_VERSION);
		writer.writeMore();
		
		writer.writeObjectField("xes.features");
		writer.writeVal("nested-attributes");
		writer.writeMore();
		
		writer.writeObjectField("openxes.version");
		writer.writeVal(XRuntimeUtils.OPENXES_VERSION);
		
		
		
		//end log-attrs object
		writer.writeObjectEnd(); 
		writer.writeMore();
		
		//begin log-children object
		writer.writeObjectField("log-attrs");
		writer.writeObjectStart();
	

		// iterate over all log attributes
		boolean firstIteration = true;
		for (XAttribute attr : log.getAttributes().values()) {
			if (firstIteration) {
				firstIteration = false;
			}else {
				writer.writeMore();
			}
			addAttr(attr,writer);
		}

		// end log-children object
		writer.writeObjectEnd();
		writer.writeMore();


		//
		
		// begin object global
		writer.writeObjectField("global");
		writer.writeObjectStart();
		
		//begin object trace
		writer.writeObjectField("trace");
		writer.writeObjectStart();
		
		//add global trace attributes
		firstIteration = true;
		for (XAttribute attr : log.getGlobalTraceAttributes()) {
			if (firstIteration) {
				firstIteration = false;
			}else {
				writer.writeMore();
			}
			addAttr(attr,writer);
		}
	
		// end object trace
		writer.writeObjectEnd();
		
		writer.writeMore();
		
		//begin object event
		writer.writeObjectField("event");
		writer.writeObjectStart();

		//add global event attributes
		firstIteration = true;
		for (XAttribute attr : log.getGlobalEventAttributes()) {
			if (firstIteration) {
				firstIteration = false;
			}else {
				writer.writeMore();
			}
			addAttr(attr,writer);
		}
		// end object event
		writer.writeObjectEnd();
		
		// end global object
		writer.writeObjectEnd();
		
		writer.writeMore();



		//begin extensions array [
		writer.writeObjectField("extensions");
		writer.writeArrayStart();
		firstIteration = true;
		for (XExtension extension : log.getExtensions()) {
			if (firstIteration) {
				firstIteration = false;
			}else {
				writer.writeMore();
			}
			// begin object for every new extension {
			writer.writeObjectStart();

			// set the name, prefix and uri for every extension
			writer.writeObjectField("name");
			writer.writeVal(extension.getName());
			writer.writeMore();
			
			writer.writeObjectField("prefix");
			writer.writeVal(extension.getPrefix());
			writer.writeMore();
			
			writer.writeObjectField("uri");
			writer.writeVal(extension.getUri().toString());

			// end object for every new extension }
			writer.writeObjectEnd();
		}

		// end extensions array ]
		writer.writeArrayEnd();
		writer.writeMore();


		//begin classifiers object {
		writer.writeObjectField("classifiers");
		writer.writeObjectStart();

		//iterate over all event classifiers 
		boolean firstIterationClassifier = true;
		for (XEventClassifier classifier : log.getClassifiers()) {
			
			if (classifier instanceof XEventAttributeClassifier) {
				if (firstIterationClassifier) {
					firstIterationClassifier = false;
				}else {
					writer.writeMore();
				}
				// get classifier object to exract names and values
				XEventAttributeClassifier attrClass = (XEventAttributeClassifier) classifier;
				
				// add name and begin array for every classifier [
				writer.writeObjectField(attrClass.name());
				writer.writeArrayStart();
				
				// get and iterate on classifier key
				firstIteration = true;
				String[] myArray = attrClass.getDefiningAttributeKeys();
				for (int i = 0; i < myArray.length; i++) {
					if (firstIteration) {
						firstIteration = false;
						
					}else {
						writer.writeMore();
					}
					// add key to array
					writer.writeVal(myArray[i]);
					
					
			    }

				writer.writeArrayEnd();
			}
		}
		//end classifiers object }
		writer.writeObjectEnd();

		writer.writeMore();


		// begin traces array
		writer.writeObjectField("traces");
		writer.writeArrayStart();

		// add all traces
		firstIteration = true;
		for (XTrace trace : log) {
			if (firstIteration) {
				firstIteration = false;
			}else {
				writer.writeMore();
			}
			compileTrace(trace,writer);
		}
		
		// end traces array
		writer.writeArrayEnd();
		
		// end the whole object
		writer.writeObjectEnd();

		writer.close();
		out.close();
		String duration = " (" + (System.currentTimeMillis() - start) + " msec.)";
		XLogging.log("finished serializing log" + duration, XLogging.Importance.DEBUG);
		System.out.println("Memory used: " +  ((double)( Runtime.getRuntime().totalMemory() -  Runtime.getRuntime().freeMemory()) / (double) (1024 * 1024)));
	}

	private void compileTrace(XTrace trace,JsonStream writer) throws IOException{
		// begin trace {
		writer.writeObjectStart();
		
		// add trace attrs
		writer.writeObjectField("attrs");
		writer.writeObjectStart();
		// iterate over all trace attirbutes
		boolean firstIteration = true;
		for (XAttribute attr : trace.getAttributes().values()){
			if (firstIteration) {
				firstIteration = false;
			}else {
				writer.writeMore();
			}
			addAttr(attr,writer);
		}
		// end trace attributes
		writer.writeObjectEnd();
		writer.writeMore();
		// add trace events
		writer.writeObjectField("events");
		writer.writeArrayStart();

		// iterate over all events in this trace
		boolean firstIterationEvent = true;
		for (ListIterator<XEvent> iterator = trace.listIterator(); iterator.hasNext();) {
			if (firstIterationEvent) {
				firstIterationEvent = false;
			}else {
				writer.writeMore();
			}
			// begin event 
			writer.writeObjectStart();
			XEvent event = iterator.next();
			firstIteration = true;
			for (XAttribute attr : event.getAttributes().values()) {
				if (firstIteration) {
					firstIteration = false;
				}else {
					writer.writeMore();
				}
				addAttr(attr,writer);
			}
			//end event 
			writer.writeObjectEnd();
			
		}
		
		//end events array
		writer.writeArrayEnd();

		//end trace object
		writer.writeObjectEnd();
		
	}


	




	protected void addAttr(XAttribute attribute, JsonStream json) throws IOException{
	
		// always set the name as first thing
		json.writeObjectField(attribute.getKey());

		// check if we are dealing with a nested attr
		boolean nested = attribute.hasAttributes();
		if(nested){
			json.writeObjectStart();
			json.writeObjectField("value");	
		}

		if (attribute instanceof XAttributeTimestamp) {
			Date timestamp = ((XAttributeTimestamp) attribute).getValue();
			json.writeVal(dateFormat.format(timestamp));
			

		} else if (attribute instanceof XAttributeDiscrete ||  attribute instanceof XAttributeContinuous ) {
			json.writeVal( Double.parseDouble(attribute.toString()));
			
		} else if (attribute instanceof XAttributeBoolean ) {
			json.writeVal( ((XAttributeBoolean) attribute).getValue());
			
		} else if (attribute instanceof XAttributeList ) {
			// get XAttributeList
			Collection<XAttribute> list = ((XAttributeList) attribute).getCollection();
			// begin the json array [
			json.writeArrayStart();
			// for every attribute in the list create a json object
			boolean firstIteration = true;
			for (XAttribute attr : list) {
				// begin the object {
				json.writeObjectStart();
				// run the method with the attribute
				if (firstIteration) {
					firstIteration = false;
				}else {
					json.writeMore();
				}
				addAttr(attr,json);
				// end the object }
				json.writeObjectEnd();
			}
			//end the json array ]
			json.writeArrayEnd();
		
		} else if (attribute instanceof XAttributeContainer ) {
			Collection<XAttribute> container = ((XAttributeContainer) attribute).getCollection();
			json.writeObjectStart();
			boolean firstIteration = true;
			for (XAttribute attr : container) {
				if (firstIteration) {
					firstIteration = false;
				}else {
					json.writeMore();
				}
				addAttr(attr,json);
			}
			json.writeObjectEnd();
		} else {
			json.writeVal(attribute.toString());
		}


		if(nested){
			// split between "value:" and "nested-attrs"
			json.writeMore();
			// add nested attributes
			json.writeObjectField("nested-attrs");
			// begin nested-attributes object
			json.writeObjectStart();
			boolean firstIteration = true;
			for(XAttribute attr : attribute.getAttributes().values()) {
				if (firstIteration) {
					firstIteration = false;
				}else {
					json.writeMore();
				}
				addAttr(attr,json);
			}
			// end nested-attributes object }
			json.writeObjectEnd();

			//end the whole object opened in the begining of the method
			json.writeObjectEnd();
		}

	}




	/**
	 * toString() defaults to getName().
	 */
	public String toString() {
		return this.getName();
	}

}