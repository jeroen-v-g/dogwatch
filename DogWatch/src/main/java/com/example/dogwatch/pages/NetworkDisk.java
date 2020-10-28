package com.example.dogwatch.pages;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.tapestry5.Block;
import org.apache.tapestry5.EventContext;
import org.apache.tapestry5.SelectModel;
import org.apache.tapestry5.ValueEncoder;
import org.apache.tapestry5.alerts.AlertManager;
import org.apache.tapestry5.alerts.Duration;
import org.apache.tapestry5.alerts.Severity;
import org.apache.tapestry5.annotations.ActivationRequestParameter;
import org.apache.tapestry5.annotations.Import;
import org.apache.tapestry5.annotations.InjectComponent;
import org.apache.tapestry5.annotations.Persist;
import org.apache.tapestry5.annotations.Property;
import org.apache.tapestry5.annotations.SessionAttribute;
import org.apache.tapestry5.corelib.components.Zone;
import org.apache.tapestry5.ioc.annotations.Inject;
import org.apache.tapestry5.services.Request;
import org.apache.tapestry5.services.SelectModelFactory;
import org.apache.tapestry5.services.ajax.AjaxResponseRenderer;
import org.apache.tapestry5.tree.DefaultTreeModel;
import org.apache.tapestry5.tree.TreeModel;
import org.apache.tapestry5.tree.TreeNode;
import org.slf4j.Logger;

import com.example.dogwatch.dao.MountOperations;
import com.example.dogwatch.model.PathAdapter;

@Import(stylesheet = "custom/css/networkDisk.css")

public class NetworkDisk {

	@Inject
	private Logger logger;

	private Path inputPath;

	@Property
	private TreeNode<Path> treeNode;
	@Property
	private SelectModel shareSelectModel;
	@Inject
	SelectModelFactory selectModelFactory;
	@ActivationRequestParameter
	@Property
	private String selectedShare;
	@Property
	private String pathName;
	@Property
	@Persist
	private HashMap<String, List<Path>> selectedPathsMap;
	@Property
	private List<Path> selectedPathsForShare;

	@Property
	private String shareName;
	@Property
	private String shareUsername;
	@Property
	private String sharePassword;
	@Inject
	private Block addShareBlock;
	@Inject
	private Block responseBlock;
	@Inject
	private AlertManager alertManager;
	@Inject
	Request request;
	@Inject
	private AjaxResponseRenderer ajaxResponseRenderer;
	@InjectComponent
	private Zone selectedPathZone;
	@InjectComponent
	private Zone treeModelZone;

	void onActivate(EventContext context) {

		if (selectedPathsMap==null)
			selectedPathsMap = new HashMap<>();
		if (selectedShare!=null) {
			try {
				inputPath = Paths.get(MountOperations.getDogWatchMountPath() + selectedShare);
				selectedPathsForShare = selectedPathsMap.computeIfAbsent(MountOperations.getDogWatchMountPath() + selectedShare,
						(name) -> new ArrayList<>());
			} catch (IOException e) {
				alertManager.alert(Duration.SINGLE, Severity.ERROR, "Initialiseren mislukt: "+e);
			}

		}

	}

	void setupRender() {
		int prefixlength = MountOperations.dogWatchMountPathName.length();
		List<String> shareList = MountOperations.getActiveMounts().stream()
				.map(string -> string.substring(prefixlength)).collect(Collectors.toList());

		shareSelectModel = selectModelFactory.create(shareList);
	}

	public List<String> getPaths() {
		return selectedPathsForShare.stream().map(path -> path.toString()).collect(Collectors.toList());
	}

	void onConfirmShare() {

		try {
			if (MountOperations.mount(shareName, shareUsername, sharePassword))
				alertManager.alert(Duration.SINGLE, Severity.SUCCESS, "Koppelen gelukt");
			else
				alertManager.alert(Duration.SINGLE, Severity.ERROR, "Koppelen mislukt");
		} catch (IOException e) {
			alertManager.alert(Duration.SINGLE, Severity.ERROR, "Koppelen mislukt: "+e);
		}

	}

	void onValidateFromShareForm() {
		if (selectedShare != null) {
			ajaxResponseRenderer.addRender(treeModelZone);
		}
	}

	void onAddShare() {
		if (request.isXHR()) {
			ajaxResponseRenderer.addRender("addShareZone", addShareBlock);
		}
	}

	void onRemoveShare() {

			try {
				if (MountOperations.unMount(MountOperations.getDogWatchMountPath() + selectedShare))
					alertManager.alert(Duration.SINGLE, Severity.SUCCESS, "Ontkoppelen gelukt");
				else
					alertManager.alert(Duration.SINGLE, Severity.ERROR, "Ontkoppelen mislukt");
			} catch (IOException e) {
				alertManager.alert(Duration.SINGLE, Severity.ERROR, "Ontkoppelen mislukt: " +e);
			}

			selectedShare = null;
		
	}

	void onLeafSelected(String pathName) {
		Path path = Paths.get(pathName);
		if (selectedPathsForShare.contains(path))
			selectedPathsForShare.remove(path);
		else
			selectedPathsForShare.add(path);

		if (request.isXHR()) {
			ajaxResponseRenderer.addRender(selectedPathZone); 
		}

	}

	public TreeModel<Path> getPathSelectModel() {
		ValueEncoder<Path> encoder = new ValueEncoder<Path>() {
			@Override
			public String toClient(Path value) {
				return value.toString();
			}

			@Override
			public Path toValue(String clientValue) {
				return Paths.get(clientValue);
			}

		};
		List<Path> root = null;
		if (inputPath != null) {
			try {
				root = Files.list(inputPath).filter(path -> Files.isDirectory(path)).collect(Collectors.toList());
			} catch (IOException e) {
				alertManager.alert(Duration.SINGLE, Severity.ERROR, "Fout in directories ophalen: "+e);
			}
		}
		PathAdapter pathadapter = new PathAdapter();
		if (root!=null)
			return new DefaultTreeModel<Path>(encoder, pathadapter, root);
		else
			return new DefaultTreeModel<Path>(encoder, pathadapter, inputPath);
	}
	
	public List<Path> getSelectedPaths()
	{
		List<Path> selectedPathsList = new ArrayList<>();
		for(String key : selectedPathsMap.keySet())
		{
			List<Path> selectedPathsForShare = selectedPathsMap.get(key);
			selectedPathsList.addAll(selectedPathsForShare);
		}
		return selectedPathsList;
			
	}
}
