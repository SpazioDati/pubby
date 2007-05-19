package de.fuberlin.wiwiss.pubby.servlets;
import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.joseki.http.ModelResponse;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.vocabulary.RDFS;

import de.fuberlin.wiwiss.pubby.Configuration;
import de.fuberlin.wiwiss.pubby.MappedResource;
import de.fuberlin.wiwiss.pubby.ResourceDescription;
import de.fuberlin.wiwiss.pubby.vocab.FOAF;

public class PathDataURLServlet extends BasePathServlet {
		
	public boolean doGet(MappedResource resource, Property property, boolean isInverse, 
			HttpServletRequest request,
			HttpServletResponse response,
			Configuration config) throws IOException {

		OutputRequestParamHandler handler = new OutputRequestParamHandler(request);
		if (handler.isMatchingRequest()) {
			request = handler.getModifiedRequest();
		}
		

		Model descriptions = getAnonymousPropertyValues(
				resource.getDatasetURI(), property, isInverse);
		if (descriptions.size() == 0) {
			return false;
		}
		
		// Add document metadata
		if (descriptions.qnameFor(FOAF.primaryTopic.getURI()) == null
				&& descriptions.getNsPrefixURI("foaf") == null) {
			descriptions.setNsPrefix("foaf", FOAF.NS);
		}
		if (descriptions.qnameFor(RDFS.label.getURI()) == null
				&& descriptions.getNsPrefixURI("rdfs") == null) {
			descriptions.setNsPrefix("rdfs", RDFS.getURI());
		}
		Resource r = descriptions.getResource(resource.getWebURI());
		Resource document = descriptions.getResource(
				isInverse ? resource.getInversePathDataURL(property) : resource.getPathDataURL(property));
		document.addProperty(FOAF.primaryTopic, r);
		String resourceLabel = new ResourceDescription(resource, descriptions, config).getLabel();
		String propertyLabel = config.getPrefixes().qnameFor(property.getURI());
		if (isInverse) {
			document.addProperty(RDFS.label, 
					"RDF description of resources whose " + propertyLabel + " is " + resourceLabel);
		} else { 
			document.addProperty(RDFS.label, 
					"RDF description of resources that are " + propertyLabel + " of " + resourceLabel);
		}
		config.addDocumentMetadata(descriptions, document);

		new ModelResponse(descriptions, request, response).serve();
		return true;
	}
}