package technology.tabula;

import java.util.ArrayList;
import java.util.List;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class TestProjectionProfile {
	
	ProjectionProfile pProfile;
	Page page;

	@BeforeEach
	public void setUpProjectionProfile() {
		PDPage pdPage = new PDPage();
		PDDocument pdDocument = new PDDocument();
		
		TextElement textElement = new TextElement(5f, 15f, 10f, 20f, PDType1Font.HELVETICA, 1f, "test", 1f);
		TextElement textElement2 = new TextElement(5f, 15f, 10f, 20f, PDType1Font.HELVETICA, 1f, "test", 1f);
		List<TextElement> textList = new ArrayList<>();
		textList.add(textElement);
		textList.add(textElement2);

		Ruling ruling = new Ruling(0, 0, 10, 10);
		List<Ruling> rulingList = new ArrayList<>();
		rulingList.add(ruling);

		page = Page.Builder.newInstance()
				.withPageDims(PageDims.of(0, 0, 1, 1))
				.withRotation(0)
				.withNumber(1)
				.withPdPage(pdPage)
				.withPdDocument(pdDocument)
				.withTextElements(textList)
				.withRulings(rulingList)
				.build();

		List<Rectangle> rectangles = new ArrayList<>();
		rectangles.add(new Rectangle(0f, 0f, 500f, 5f));
		
		pProfile = new ProjectionProfile(page, rectangles, 5, 5);
	}

	@Test
	public void testGetVerticalProjection() {
		float[] projection = pProfile.getVerticalProjection();
        assertEquals(10, projection.length);
		}

	@Test
	public void testGetHorizontalProjection() {
		float[] projection = pProfile.getHorizontalProjection();
        assertEquals(10, projection.length);
	}

	@Test
	public void testFindVerticalSeparators() {
		float[] seperators = pProfile.findVerticalSeparators(page.getText().size() * 2.5f);
        assertEquals(0, seperators.length);
	}

	@Test
	public void testFindHorizontalSeparators() {
		float[] seperators = pProfile.findHorizontalSeparators(page.getText().size() * 2.5f);
        assertEquals(0, seperators.length);
	}

	@Test
	public void testSmooth() {
		float[] data = {0, 1, 2};
		float[] rv = ProjectionProfile.smooth(data, 3);

		assertEquals(1f, rv[2], 1e-5);
	}

	@Test
	public void testFilter() {
		float[] data = {0, 1, 2};
		float[] rv = ProjectionProfile.filter(data, 3);

		assertEquals(3f, rv[1], 1e-5);
		}

	@Test
	public void testGetAutocorrelation() {
		float[] projection = {0, 1, 2};
		float[] rv = ProjectionProfile.getAutocorrelation(projection);

		assertEquals(0f, rv[0], 1e-5);
        assertEquals(2, rv.length);

	}

	@Test
	public void testGetFirstDeriv() {
//		float[]
//		float[] projection = pProfile.getFirstDeriv(new float[]{0.0, 0.0)
//		System.out.println(Arrays.toString(projection));    
//		assertEquals(10, projection[0], 1e-15);
		}

}
