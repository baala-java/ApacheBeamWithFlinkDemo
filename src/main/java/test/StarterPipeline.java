/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package test;

import org.apache.beam.sdk.values.PCollection;

import java.io.StringWriter;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;

import org.apache.beam.sdk.Pipeline;
import org.apache.beam.sdk.io.TextIO;
import org.apache.beam.sdk.options.PipelineOptions;
import org.apache.beam.sdk.options.PipelineOptionsFactory;
import org.apache.beam.sdk.transforms.DoFn;
import org.apache.beam.sdk.transforms.ParDo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A starter example for writing Beam programs.
 *
 * <p>
 * The example takes two strings, converts them to their upper-case
 * representation and logs them.
 *
 * <p>
 * To run this starter example locally using DirectRunner, just execute it
 * without any additional parameters from your favorite development environment.
 *
 * <p>
 * To run this starter example using managed resource in Google Cloud Platform,
 * you should specify the following command-line options:
 * --project=<YOUR_PROJECT_ID>
 * --stagingLocation=<STAGING_LOCATION_IN_CLOUD_STORAGE> --runner=DataflowRunner
 */
public class StarterPipeline {
	private static final Logger LOG = LoggerFactory.getLogger(StarterPipeline.class);
    final static int SLEEP_TIME = 5000;
    
    static class Converter extends DoFn<String, String> {
    	
        @Setup
        public void setup() throws Exception {
        	System.out.println("setup");
        }

        @ProcessElement
        public void processElement(ProcessContext context) throws Exception {
        	System.out.println("processElement");
			String[] split = context.element().split(",");
			Car car = new Car();
			car.setId(Integer.parseInt(split[0]));
			car.setBrand(split[1]);
			car.setModel(split[2]);
			car.setTotal(split[3]);
			context.output(getXmlString(car));
        }

        @FinishBundle
        public void finishBundle() throws InterruptedException {
        	System.out.println("finishBundle");
           // runLongMethod("finishBundle()");
        }

        @Teardown
        public void teardown() throws InterruptedException {
        	System.out.println("finishBundle");
            //runLongMethod("teardown()");
        }
        


        private void runLongMethod(String name) throws InterruptedException {
            long beginTs = System.currentTimeMillis();
            long tId = Thread.currentThread().getId();
            System.out.println("Thread #" + tId + ", call " + name);
            Thread.sleep(SLEEP_TIME);
            long endTs = System.currentTimeMillis();
            System.out.println("Thread #" + tId +  ", run for " + (endTs - beginTs) + " ms");
        }
    }

	public static void main(String[] args) {
		for(String arg: args) {
			System.out.println(arg);
		}
		PipelineOptions pipelineOptions = PipelineOptionsFactory.fromArgs(args).withValidation().create();
		
		Pipeline pipeline = Pipeline.create(pipelineOptions);

	    PCollection<String> input = pipeline.apply(TextIO.read().from("/home/bala/eclipse-workspace/test/src/main/resources/input.data"));
	    PCollection<String> output =input.apply(
	            "CSVToXML",
	            ParDo.of(new Converter()));
	    output.apply(TextIO.write().to("/home/bala/eclipse-workspace/test/src/main/resources/output.xml").withoutSharding());

	    pipeline.run();
	}

	protected static String getXmlString(Car car) {
		JAXBContext jaxbContext;
		StringWriter sw = new StringWriter();
		try {
			jaxbContext = JAXBContext.newInstance(Car.class);

			Marshaller jaxbMarshaller = jaxbContext.createMarshaller();

			// output pretty printed
			jaxbMarshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
			jaxbMarshaller.setProperty(Marshaller.JAXB_FRAGMENT, Boolean.TRUE);
			jaxbMarshaller.marshal(car, sw);
		} catch (JAXBException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return sw.toString();
	}
}
