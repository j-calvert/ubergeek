import java.awt.Color;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

import name.audet.samuel.javacv.CanvasFrame;
public class Test extends CanvasFrame {
    ViewPanel vp = null;
    Test(){
        super(false);
        
        this.vp = new ViewPanel();
        this.vp.setBackground(Color.BLACK );
        
        this.setBounds( 100, 100, 320, 240);
        this.add(this.vp);
        this.setVisible(true);
        this.createBufferStrategy(4);
		this.addKeyListener(
				new KeyAdapter() {
					public void keyReleased( KeyEvent e ) { 
						if ( e.getKeyCode()==KeyEvent.VK_SPACE ) // SPACE : record background
							vp.remember();
					}
				}
			);
			

        this.vp.start();    
    }
} 