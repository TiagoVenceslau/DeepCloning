<h1>Deep Cloning Implementation</h1>
<h5>with property update via user defined logic</h5>
<br>
<p>Simple way to quickly implement a custom deep cloning solution, 
with minimal impact to the project and the possibility to apply 
user defined logic the update required properties of the clones.</p>
<p><strong>Primitives will remain the same</strong></p>
<br>
<p>Allows for easy customization of the update logic either by creating new
UpdateSpecifications, or rewriting the update functions.</p>
<p>Originally the update method only worked for strings, I refactored in the UpdateSpecifications
to allow to update any non-primitive type</p>
<br>
<p>Licenced via MIT licence</p>
