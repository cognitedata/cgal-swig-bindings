%module Polyhedron_3

%include "../common.i"
%import  "../Kernel/Point_3.h"
%import  "../Kernel/Plane_3.h"
%import  "../Common/Macros.h"
%import  "../Common/Input_iterator.h"

//include files
%{
  #include "Polyhedron_3.h"
  #include "polyhedron_3_handles.h"
  #include "polyhedron_3_iterators.h"
%}

%pragma(java) jniclassimports=%{import CGAL.Kernel.Point_3;%}


//definitions
%include "Polyhedron_3.h"
%include "polyhedron_3_handles.h"
%include "polyhedron_3_iterators.h"


//template instantiation
%template(Polyhedron_3_Halfedge_handle)             CGAL_Halfedge_handle<Polyhedron>;
%typemap(javaimports)                               CGAL_Vertex_handle%{import CGAL.Kernel.Point_3;%}
%template(Polyhedron_3_Vertex_handle)               CGAL_Vertex_handle<Polyhedron>;
%typemap(javaimports)                               CGAL_Facet_handle%{import CGAL.Kernel.Plane_3;%}
%template(Polyhedron_3_Facet_handle)                CGAL_Facet_handle<Polyhedron>;

%typemap(javaimports)                     Polyhedron_3 %{import CGAL.Kernel.Point_3;%}
%template(Polyhedron)                     Polyhedron_3<Polyhedron,CGAL_Vertex_handle<Polyhedron>,CGAL_Halfedge_handle<Polyhedron>,CGAL_Facet_handle<Polyhedron> >;

//Iterators
Iterator_for_java(CGAL_Halfedge_iterator,Polyhedron_3_Halfedge_handle,;)
%template(Polyhedron_3_Halfedge_iterator) CGAL_Halfedge_iterator<Polyhedron,CGAL_Halfedge_handle<Polyhedron> >;

Iterator_for_java(CGAL_Edge_iterator,Polyhedron_3_Halfedge_handle,;)
%template(Polyhedron_3_Edge_iterator) CGAL_Edge_iterator<Polyhedron,CGAL_Halfedge_handle<Polyhedron> >;

Iterator_for_java(CGAL_Vertex_iterator,Polyhedron_3_Vertex_handle,;)
%template(Polyhedron_3_Vertex_iterator) CGAL_Vertex_iterator<Polyhedron,CGAL_Vertex_handle<Polyhedron> >;

Iterator_for_java(CGAL_Facet_iterator,Polyhedron_3_Facet_handle,;)
%template(Polyhedron_3_Facet_iterator) CGAL_Facet_iterator<Polyhedron,CGAL_Facet_handle<Polyhedron> >;

Iterator_for_java(CGAL_Point_iterator,Point_3,import CGAL.Kernel.Point_3;)
%template(Polyhedron_3_Point_iterator) CGAL_Point_iterator<Polyhedron,Point_3>; 

Iterator_for_java(CGAL_Plane_iterator,Plane_3,import CGAL.Kernel.Plane_3;)
%template(Polyhedron_3_Plane_iterator) CGAL_Plane_iterator<Polyhedron,Plane_3>; 

Iterator_for_java(CGAL_Halfedge_around_vertex_circulator,Polyhedron_3_Halfedge_handle,;)
%template(Polyhedron_3_Halfedge_around_vertex_circulator) CGAL_Halfedge_around_vertex_circulator<Polyhedron,CGAL_Halfedge_handle<Polyhedron> >;

Iterator_for_java(CGAL_Halfedge_around_facet_circulator,Polyhedron_3_Halfedge_handle,;)
%template(Polyhedron_3_Halfedge_around_facet_circulator) CGAL_Halfedge_around_facet_circulator<Polyhedron,CGAL_Halfedge_handle<Polyhedron> >;
