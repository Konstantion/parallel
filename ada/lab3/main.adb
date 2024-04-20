with Ada.Text_IO;
use Ada.Text_IO;
with Ada.Integer_Text_IO;
use Ada.Integer_Text_IO;
with Ada.Containers.Doubly_Linked_Lists;
use Ada.Containers.Doubly_Linked_Lists;
with GNAT.Semaphores;
use GNAT.Semaphores;

procedure Main is
    package Integer_Lists is new Doubly_Linked_Lists (Element_Type => Integer);

    procedure Storage (Capacity : in Integer) is
        Items_Storage   : Integer_Lists.List;
        Space_Available : GNAT.Semaphores.Counting_Semaphore := GNAT.Semaphores.Counting_Semaphore'(Initial => Capacity, Maximum => Capacity);
        Items_Available : GNAT.Semaphores.Counting_Semaphore := GNAT.Semaphores.Counting_Semaphore'(Initial => 0, Maximum => Capacity);

        task type Producer is
            entry Start (ID : Integer; Items_To_Produce : Integer);
        end Producer;

        task type Consumer is
            entry Start (ID : Integer; Items_To_Consume : Integer);
        end Consumer;

        task body Producer is
            My_ID            : Integer;
            Items_To_Produce : Integer;
        begin
            accept Start (ID : Integer; Items_To_Produce : Integer) do
                Producer.My_ID := ID;
                Producer.Items_To_Produce := Items_To_Produce;
            end Start;

            for I in 1 .. Items_To_Produce loop
                Space_Available.Wait;
                Items_Storage.Append (I);
                Put_Line ("Producer " & Integer'Image(My_ID) & " added item: " & Integer'Image(I));
                Items_Available.Signal;
            end loop;
        end Producer;

        task body Consumer is
            My_ID            : Integer;
            Items_To_Consume : Integer;
        begin
            accept Start (ID : Integer; Items_To_Consume : Integer) do
                Consumer.My_ID := ID;
                Consumer.Items_To_Consume := Items_To_Consume;
            end Start;

            for I in 1 .. Items_To_Consume loop
                Items_Available.Wait;
                declare
                    Item : Integer := Integer_Lists.First_Element(Items_Storage);
                begin
                    Put_Line("Consumer " & Integer'Image(My_ID) & " removed item: " & Integer'Image(Item));
                    Items_Storage.Delete_First;
                end;
                Space_Available.Signal;
            end loop;
        end Consumer;

        Producers : array (1 .. 5) of Producer;
        Consumers : array (1 .. 3) of Consumer;

    begin
        for I in Producers'Range loop
            Producers(I).Start(I, 15); -- Producers start with ID and Items to Produce
        end loop;

        for J in Consumers'Range loop
            Consumers(J).Start(J, 25); -- Consumers start with ID and Items to Consume
        end loop;
    end Storage;

begin
    Storage(20); -- Initialize storage with capacity
end Main;

