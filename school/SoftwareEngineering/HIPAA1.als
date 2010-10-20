open util/boolean

///////////////////////////////// Atoms /////////////////////////////////

// Base class for Person, CoveredEntity, or agency.
// The generalization is used once to identify a thing
// that can know a piece of information.
abstract sig Entity { 
	knowns : set Info
} 

// A person can be a subject of a piece of information (see definition
// of Info below), a posessor of a piece of information (by way of 
// being an entity), a personal representative of another person,
// or an employee of an organization.
sig Person extends Entity {
	// The personal representatives of this person, which we take to be
	// people which are entitled to any info for which this person is the 
	// subject if and only if this person is (namely, if and only if it is not
	// PsychInfo.
	reps: set Person,
	// The authorizations given to CoveredEntities to share information
	// We use the domain of this mapping identify CoveredEntities
	// that have a claim to any information regarding this person
	// (e.g. these are the person's insurers and providers) within the
	// principle of Minimun Necessary Disclosure.
	providers : set CoveredEntity
}


// An entity that consists of employees. 
abstract sig Organization extends Entity {
	employees : set Person
}

// A thing that can own a piece of info and is responsible for the protection
// of its privacy, for example, Health Care Provider, Clearinghouse, or 
// Insurer.
//
// Providing information about p to o in accordance to the
// principle of Minimum Necessary Disclosure (MND) requires this,
// regardless of who is providing the info.
//
// Accordance with MND is only necessary when o is a CoveredEntity, and 
// only covered entities that are in the domain of the person's auth map,
// interpreted as meaning the person is a client of the covered entity,
// can have information about that person according to the principle
// of MND.
sig CoveredEntity extends Organization {
	auths : set Person
}

// An entity that does not need authorization to have a piece
// of information owned by another entity, for example, Law Enforcement
// or HHS.
sig Agency extends Organization { }

// A piece of information can be shared by multiple entities, by way of
// it being present in that entity's "knowns" set.  _A_ posessor of the info
// should not be confused with _the_ owner of the info.
abstract sig Info {
	// The subject of this piece of info
	subject : one Person,
	// The owner of this piece of info, who needs to have had authorization
	// if it ends up being known by a non-employee or non-subject.
	owner : one CoveredEntity,
	// Is this info psychiatric, meaning the subject is not permitted to know it.
	isPsych : one Bool
}

///////////////////////////////// Facts /////////////////////////////////

// This isn't absolutely necessary, but not asserting it doesn't introduce
// any more interesting cases, and asserting it prevents degenerate edges
// in our entity digraph.
fact {no p:Person | p in p.reps }

// A person has access to all information about which she is the
// subject, provided it is not psychiatric info
fact personHasAllNonPsychSelfInfo {
	all p: Person, e: Entity, i: Info |
		(i in e.knowns and i.subject = p and i.isPsych = False) 	
			=> i in p.knowns
}

//  A person can not be the posessor of a piece psychiatric info 
// of which she is the subject.
fact personHasNoPsychSelfInfo {
	all p: Person, i: Info |
		(i in p.knowns and i.subject = p) => i.isPsych = False
}

// An organization posseses all info of which it is the owner, and the
// owner of a piece of information must have the subject of the info
// as a client.
fact orgHasAllOwnedInfo {
	all o: Organization, i: Info |
		// i in Entity.knowns is another way of saying the information exists
		(i in Entity.knowns and i.owner = o)
			=> (i in o.knowns and o in i.subject.providers)
}


/////////////////////////////// Predicates //////////////////////////////

// True if person p can share a piece of information with a given
// organization o.   Note, that this is not a function of the piece of
// info.
//
// Either the two are employees of the same organization, 
// or p1 can share their information with their personal representative p2
// or p1 can share p2's information with p2, of which they're a personal
// representative (so this predicate is symmetric in the Person args).
pred personCanShareWithPerson [p1, p2 : Person, i : Info ]{
	(some o: Organization | p1 + p2 in o.employees)
    or (i.subject = p1 and p2 in p1.reps)
	or (i.subject = p2 and p1 in p2.reps)
}

pred personDoesShareWithPerson[p1, p2 : Person, i : Info ] {
	personCanShareWithPerson[p1, p2, i] => i in p2.knowns 
}


// True if organization o can share piece of information i with 
// a person p.
//
// Either p is an employee of o, or the org is a covered
// entity and p is the subject or a personal representative
// of the subject.
pred orgCanShareWithPerson[o : Organization, p: Person, i: Info] {
	// person works for the org
	p in o.employees
	or (some c : CoveredEntity |
			// org is a covered enitity
			( c = o 
				// not psych info
				and i.isPsych = False
				// person is subject or rep of subject
				and (i.subject = p or p in i.subject.reps)
			)
		)
}

// True if organization o1 can share a piece of information i
// with organization o2.
//
// Either the org is an agency, or o1 is a CoveredEntity and has
// the subject's authorization and the subject is a client of o2.
pred orgCanShareWithOrg[o1, o2: Organization, i: Info]{
	o2 in Agency
	or
		(i.subject in o1.auths
		and o2 in i.subject.providers)
}

/////////////////////////////// Assertions //////////////////////////////

// Person has any information shared with it by a person
// based on the above predicates

assert personsShareWithPersons {
	all p1, p2 : Person, i: Info |
	personCanShareWithPerson[p1, p2, i] =>
		personDoesShareWithPerson[p1, p2, i]
}

check personsShareWithPersons 

// Forbid a person or any of her representatives from being an employee
// of any organization of which she's a client.
//fact {all o : Organization, p : Person |
//	o in p.providers => (p + p.reps) & o.employees = none
//}

fact {no p : Person | p in (Agency.employees & Info.subject) }

