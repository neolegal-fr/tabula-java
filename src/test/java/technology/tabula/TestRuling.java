package technology.tabula;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class TestRuling {
	
	Ruling ruling;
	
	@BeforeEach
	public void setUpRuling() {
		ruling = new Ruling(0, 0, 10, 10);
	}

	@Test
	public void testGetWidth() {
		assertEquals(10f, ruling.getWidth(), 1e-5);
	}

	@Test
	public void testGetHeight() {
		assertEquals(10f, ruling.getHeight(), 1e-5);
	}

	@Test
	public void testToString() {
		assertEquals("class technology.tabula.Ruling[x1=0.000000 y1=0.000000 x2=10.000000 y2=10.000000]",ruling.toString());
	}
	
	@Test
	public void testEqualsOther() {
		Ruling other = new Ruling(0, 0, 11, 10);
        assertEquals(ruling, ruling);
	}
	
	@Test
	public void testEqualsDifferentInstance() {
        assertNotEquals("test", ruling);
	}
	
	@Test
	public void testNearlyIntersects(){
		Ruling another = new Ruling(0, 0, 11, 10);

		assertTrue(ruling.nearlyIntersects(another));
	}
	
	@Test
	public void testGetPositionError(){
		Ruling other = new Ruling(0, 0, 1, 1);
		assertThrows(UnsupportedOperationException.class, () -> other.getPosition());
	}
	
	@Test
	public void testSetPositionError(){
		Ruling other = new Ruling(0, 0, 1, 1);
		assertThrows(UnsupportedOperationException.class, () -> other.setPosition(5f));
	}
	
	@Test
	public void testsetPosition(){
		assertThrows(UnsupportedOperationException.class, () -> ruling.setPosition(0));;
	}
	
	@Test
	public void testGetStartError(){
		Ruling other = new Ruling(0, 0, 1, 1);
		assertThrows(UnsupportedOperationException.class, () -> other.getStart());
	}
	
	@Test
	public void testGetEndError(){
		Ruling other = new Ruling(0, 0, 1, 1);
		assertThrows(UnsupportedOperationException.class, () -> other.getEnd());
	}
	
	@Test
	public void testSetEndError(){
		Ruling other = new Ruling(0, 0, 1, 1);
		assertThrows(UnsupportedOperationException.class, () -> other.setEnd(5f));
	}
	
	
	@Test
	public void testColinear(){
//		Ruling another = new Ruling(0, 0, 500, 5);
		java.awt.geom.Point2D.Float float1 = new java.awt.geom.Point2D.Float(20, 20);
		java.awt.geom.Point2D.Float float2 = new java.awt.geom.Point2D.Float(0, 0);
		java.awt.geom.Point2D.Float float3 = new java.awt.geom.Point2D.Float(20, 0);
		java.awt.geom.Point2D.Float float4 = new java.awt.geom.Point2D.Float(0, 20);
		
		assertFalse(ruling.colinear(float1));
		assertTrue(ruling.colinear(float2));
		assertFalse(ruling.colinear(float3));
		assertFalse(ruling.colinear(float4));


	}

}
