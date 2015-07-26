package org.jenkner.wicket;

import org.apache.wicket.extensions.wizard.Wizard;
import org.apache.wicket.extensions.wizard.WizardModel;
import org.apache.wicket.extensions.wizard.WizardStep;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.upload.FileUpload;
import org.apache.wicket.markup.html.form.upload.FileUploadField;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.util.lang.Bytes;
import org.apache.wicket.validation.IValidatable;
import org.apache.wicket.validation.IValidator;
import org.apache.wicket.validation.ValidationError;

import java.io.IOException;
import java.io.Serializable;
import java.nio.file.CopyOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class HomePage extends WebPage {
	private static final long serialVersionUID = 1L;

	public HomePage(final PageParameters parameters) {
		super(parameters);

		add(new UploadWizard("wizard"));

    }

	private class UploadDto implements Serializable {
		private Path excelFile;
		private List<FileUpload> fileUpload;

		public List<FileUpload> getFileUpload() {
			return fileUpload;
		}

		public void setFileUpload(final List<FileUpload> fileUpload) {
			this.fileUpload = fileUpload;
			if (null != fileUpload) {
				for (final FileUpload f : fileUpload) {
					try {
						excelFile = Files.createTempFile("excel_import", ".xlsx");
						Files.copy(f.getInputStream(), excelFile, CopyOption);
						f.closeStreams();
					} catch (IOException e) {
						e.printStackTrace();
					}
                }
			}
		}

		public Path getExcelFile() {
			return excelFile;
		}
	}

	private class UploadWizard extends Wizard {
		public UploadWizard(final String id) {
			super(id);
			setDefaultModel(new CompoundPropertyModel<UploadDto>(new UploadDto()));

			final WizardModel wizardModel = new WizardModel();
			wizardModel.add(new FileUploadStep());
			wizardModel.add(new SummaryStep());

			init(wizardModel);
		}

		@Override
        protected <E> Form<E> newForm(final String id) {
            final Form<E> form = super.newForm(id);
            form.setMultiPart(true);
            form.setMaxSize(Bytes.megabytes(10));
            return form;
        }

		private class FileUploadStep extends WizardStep {
			public FileUploadStep() {
				final FileUploadField fileUploadField = new FileUploadField("fileUpload");
				fileUploadField.setRequired(true);
				fileUploadField.add(new IValidator<List<FileUpload>>() {
					@Override
					public void validate(final IValidatable<List<FileUpload>> validatable) {
						final String contentType = validatable.getValue().get(0).getContentType();
						if (!"application/vnd.openxmlformats-officedocument.spreadsheetml.sheet".equals(contentType)) {
							final ValidationError validationError = new ValidationError("Unknown content-type: " + contentType);
							validationError.setVariable("contentType", contentType);
							validatable.error(validationError);
						};
					}
				});
				add(fileUploadField);
			}
		}

		private class SummaryStep extends WizardStep {
			public SummaryStep() {
				add(new Label("excelFile.fileName"));
			}
		}

	}



}
