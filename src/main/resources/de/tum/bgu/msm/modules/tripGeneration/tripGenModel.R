# EXAMPLE CODE: Save everything to a .rda file (takes a long time!)
rdaFileName <- paste(purpose, "_modelData.rda", sep = "")
print(paste("R: Saving full image to ",getwd(),"/",rdaFileName))
save.image(file = rdaFileName)
print(paste("R: purpose",purpose,"DONE"))

