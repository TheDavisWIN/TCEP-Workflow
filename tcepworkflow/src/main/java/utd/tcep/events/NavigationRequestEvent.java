/***********************************************************************************************************************
 * Custom event object to handle row clicking to open forms
 * Written by Ryan Pham (rkp200003)
***********************************************************************************************************************/

package utd.tcep.events;

import javafx.event.*;
import utd.tcep.data.TCEPForm;

public class NavigationRequestEvent extends Event {

    private TCEPForm form;

    public static final EventType<NavigationRequestEvent> REQUEST = new EventType<>(Event.ANY, "REQUEST");

    public NavigationRequestEvent(TCEPForm form) {
        super(NavigationRequestEvent.REQUEST);
        this.form = form;
    }

    public TCEPForm getForm() {
        return form;
    }
}