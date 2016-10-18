package org.verapdf;

import org.verapdf.core.FeatureParsingException;
import org.verapdf.features.AbstractFontFeaturesExtractor;
import org.verapdf.features.FontFeaturesData;
import org.verapdf.features.tools.FeatureTreeNode;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Maksim Bezrukov
 */
public class FontTypeExtractor extends AbstractFontFeaturesExtractor {

    private static final Logger LOGGER = Logger.getLogger(FontTypeExtractor.class.getCanonicalName());

	private static final byte[] OPENTYPE_BEGIN = new byte[]{0x4f, 0x54, 0x54, 0x4f};
	private static final byte[] PS_TYPE1_BEGIN = new byte[]{0x25, 0x21};
	private static final byte[] TRUE_TYPE_BEGIN = new byte[]{0x00, 0x01, 0x00, 0x00};
	private static final byte[] TRUE_TYPE_TRUE_BEGIN = new byte[]{0x74, 0x72, 0x75, 0x65};

	private static final String UNDEFINED = "Undefined";

	@Override
	public List<FeatureTreeNode> getFontFeatures(FontFeaturesData fontFeaturesData) {
		List<FeatureTreeNode> res = new ArrayList<>();

		try {
			String fontType = getFontType(fontFeaturesData.getStream());
			FeatureTreeNode fontTypeFromFile = FeatureTreeNode.createRootNode("fontTypeFromFile");
			fontTypeFromFile.setValue(fontType);
			res.add(fontTypeFromFile);
		} catch (FeatureParsingException | IOException e) {
			LOGGER.log(Level.WARNING, "Exception extracting font features", e);
		}

		return res;
	}

	private static String getFontType(InputStream file) throws IOException {
		byte[] firstFour = getFirstFourBytes(file);

		if (startsWith(firstFour, PS_TYPE1_BEGIN)) {
			return "PS Type1";
		} else if (startsWith(firstFour, OPENTYPE_BEGIN)) {
			return "OpenType";
		} else if (startsWith(firstFour, TRUE_TYPE_BEGIN) || startsWith(firstFour, TRUE_TYPE_TRUE_BEGIN)) {
			return "TrueType";
		} else if (firstFour[0] == 1 && (firstFour[1] >= 0 && firstFour[1] <= 5)) {
			return "CFF Type1";
		} else {
			return UNDEFINED;
		}
	}

	private static boolean startsWith(byte[] orig, byte[] pattern) {
		if (orig.length < pattern.length) {
			return false;
		}
		for (int i = 0; i < pattern.length; ++i) {
			if (orig[i] != pattern[i]) {
				return false;
			}
		}
		return true;
	}

	private static byte[] getFirstFourBytes(InputStream is) throws IOException {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		byte[] bytes = new byte[4];
		int obtainedBytes = 0;
		int length;
		while ((length = is.read(bytes)) != -1 || obtainedBytes != 4) {
			baos.write(bytes, 0, length);
			obtainedBytes += length;
		}
		return baos.toByteArray();
	}
}
