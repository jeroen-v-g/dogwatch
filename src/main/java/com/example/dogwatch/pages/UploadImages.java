package com.example.dogwatch.pages;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.tapestry5.alerts.AlertManager;
import org.apache.tapestry5.alerts.Duration;
import org.apache.tapestry5.alerts.Severity;
import org.apache.tapestry5.annotations.Component;
import org.apache.tapestry5.annotations.Property;
import org.apache.tapestry5.corelib.components.Form;
import org.apache.tapestry5.ioc.Messages;
import org.apache.tapestry5.ioc.annotations.Inject;
import org.apache.tapestry5.services.PersistentLocale;
import org.apache.tapestry5.upload.services.UploadedFile;

import com.example.dogwatch.dao.ImageTransfer;

public class UploadImages
{

	@Property 
	String searchImage;

	@Component
	Form uploadForm;
	
	@Inject
	private AlertManager alertManager;
	
    @Property
    private UploadedFile file;

    @Inject
    private Messages messages;
    
    @Inject 
    private PersistentLocale persistentLocale;
    
    public void onSuccess()
    {     
        try {
			ImageTransfer.writeFile(file.getStream(), file.getFileName());
		} catch (IOException e) {
			alertManager.alert(Duration.SINGLE, Severity.ERROR, messages.get("error-uploading")+ ": "+e);
		}
    }
    
    public void onValidate()
    {
    	try {
			if (file!=null && ImageTransfer.getSearchImages().contains(file.getFileName()))
			{
				uploadForm.recordError("File already uploaded");
			}
		} catch (IOException e) {
			alertManager.alert(Duration.SINGLE, Severity.ERROR, messages.get("error-uploading")+ ": "+e);
		}
    }
	
	public List<String> getSearchImages()
	{
		try {
			return ImageTransfer.getSearchImages();
		} catch (IOException e) {
			alertManager.alert(Duration.SINGLE, Severity.ERROR, messages.get("error-retrieving")+ ": "+e);
		}
		return new ArrayList<String>();
	}
	
	void onActionFromRemoveImage(String imageName)
	{
		try {
			ImageTransfer.removeImage(imageName);
		} catch (IOException e) {
			alertManager.alert(Duration.SINGLE, Severity.ERROR, messages.get("error-deleting")+ ": "+e);
		}

	}
}
